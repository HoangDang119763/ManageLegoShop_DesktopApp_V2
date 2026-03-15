USE java_sql;
SET GLOBAL event_scheduler = ON;
DROP PROCEDURE IF EXISTS sp_CalculateMonthlyPayroll;
DELIMITER $$

CREATE PROCEDURE sp_CalculateMonthlyPayroll(
    IN p_employee_id INT,
    IN p_salary_period DATE
)
BEGIN
    -- 1. KHAI BÁO BIẾN (Tất cả mặc định là 0 để tránh NULL)
    DECLARE v_base_salary DECIMAL(15,2) DEFAULT 0;
    DECLARE v_standard_days INT DEFAULT 26;
    DECLARE v_sundays INT DEFAULT 0;
    DECLARE v_holidays INT DEFAULT 0;
    DECLARE v_days_in_month INT DEFAULT 30;
    DECLARE v_dependents INT DEFAULT 0;
    DECLARE v_actual_days DECIMAL(10,2) DEFAULT 0;

    DECLARE v_bhxh, v_bhyt, v_bhtn, v_total_insurance DECIMAL(15,2) DEFAULT 0;
    DECLARE v_has_bhxh, v_has_bhyt, v_has_bhtn VARCHAR(50);
    DECLARE v_is_meal, v_is_accommodation, v_is_transport VARCHAR(50);

    DECLARE v_violation, v_leave_fine, v_allowance DECIMAL(15,2) DEFAULT 0;
    DECLARE v_meal_amount, v_accommodation_amount, v_transport_amount DECIMAL(15,2) DEFAULT 0;
    
    DECLARE v_ot_hours DECIMAL(10,2) DEFAULT 0;
    DECLARE v_ot_money DECIMAL(15,2) DEFAULT 0;

    DECLARE v_gross_income, v_taxable_income, v_tax_calc_basis, v_tax_amount, v_net_salary DECIMAL(15,2) DEFAULT 0;

    -- 2. LẤY THÔNG TIN NHÂN VIÊN (Dùng IFNULL để chặn NULL từ bảng)
    SELECT 
        IFNULL(p.wage, 0), IFNULL(e.num_dependents, 0),
        IFNULL(e.is_meal_support, 0), IFNULL(e.is_accommodation_support, 0), IFNULL(e.is_transportation_support, 0),
        e.social_insurance_code, e.health_insurance_code, e.unemployment_insurance_code
    INTO 
        v_base_salary, v_dependents,
        v_is_meal, v_is_accommodation, v_is_transport,
        v_has_bhxh, v_has_bhyt, v_has_bhtn
    FROM employee e
    JOIN position p ON e.position_id = p.id
    WHERE e.id = p_employee_id;

    -- 3. TÍNH TOÁN NGÀY CÔNG
    SET v_days_in_month = DAY(LAST_DAY(p_salary_period));
    
    -- Đếm Chủ Nhật (Recursive CTE an toàn cho mọi tháng)
    WITH RECURSIVE days AS (
        SELECT p_salary_period AS d
        UNION ALL
        SELECT d + INTERVAL 1 DAY FROM days WHERE d + INTERVAL 1 DAY <= LAST_DAY(p_salary_period)
    )
    SELECT COUNT(*) INTO v_sundays FROM days WHERE DAYOFWEEK(d) = 1;

    -- Đếm ngày lễ
    SELECT COUNT(*) INTO v_holidays FROM holiday 
    WHERE MONTH(date) = MONTH(p_salary_period) AND YEAR(date) = YEAR(p_salary_period) AND DAYOFWEEK(date) <> 1;

    -- Ngày công chuẩn (Thường từ 24-26 ngày)
    SET v_standard_days = GREATEST(1, v_days_in_month - v_sundays - v_holidays);

    -- Ngày công thực tế & Giờ OT
    SELECT 
        IFNULL(COUNT(DISTINCT DATE(check_in)), 0), 
        IFNULL(SUM(ot_hours), 0)
    INTO v_actual_days, v_ot_hours
    FROM time_sheet
    WHERE employee_id = p_employee_id 
    AND MONTH(check_in) = MONTH(p_salary_period) AND YEAR(check_in) = YEAR(p_salary_period);

    -- Tiền OT (Lương/Ngày chuẩn/8 tiếng * giờ * 1.5)
    SET v_ot_money = (v_base_salary / v_standard_days / 8) * v_ot_hours * 1.5;

    -- 4. PHỤ CẤP & PHẠT
    IF v_is_meal = 1 THEN SELECT IFNULL(amount, 0) INTO v_meal_amount FROM allowance WHERE id = 1; END IF;
    IF v_is_accommodation = 1 THEN SELECT IFNULL(amount, 0) INTO v_accommodation_amount FROM allowance WHERE id = 2; END IF;
    IF v_is_transport = 1 THEN SELECT IFNULL(amount, 0) INTO v_transport_amount FROM allowance WHERE id = 3; END IF;
    
    SET v_allowance = v_meal_amount + v_accommodation_amount + v_transport_amount;

    SELECT IFNULL(SUM(amount), 0) INTO v_violation FROM fine 
    WHERE employee_id = p_employee_id AND MONTH(created_at) = MONTH(p_salary_period) AND YEAR(created_at) = YEAR(p_salary_period);

    SELECT IFNULL(SUM(lt.fine_amount), 0) INTO v_leave_fine FROM leave_request lr
    JOIN leave_type lt ON lr.leave_type_id = lt.id
    WHERE lr.employee_id = p_employee_id AND lr.status_id = 20 
    AND MONTH(lr.start_date) = MONTH(p_salary_period) AND YEAR(lr.start_date) = YEAR(p_salary_period);

    -- 5. BẢO HIỂM
    IF v_actual_days >= (v_standard_days / 2) THEN
        IF v_has_bhxh IS NOT NULL AND v_has_bhxh <> '0' THEN SET v_bhxh = v_base_salary * 0.08; END IF;
        IF v_has_bhyt IS NOT NULL AND v_has_bhyt <> '0' THEN SET v_bhyt = v_base_salary * 0.015; END IF;
        IF v_has_bhtn IS NOT NULL AND v_has_bhtn <> '0' THEN SET v_bhtn = v_base_salary * 0.01; END IF;
    END IF;
    SET v_total_insurance = v_bhxh + v_bhyt + v_bhtn;

    -- 6. TÍNH TOÁN LƯƠNG & THUẾ (CHẶN SỐ ÂM & NULL)
    
    -- Gross = (Lương/Ngày chuẩn * Ngày thực) + OT + Phụ cấp - Phạt nghỉ
    SET v_gross_income = GREATEST(0, ((v_base_salary / v_standard_days) * v_actual_days) + v_ot_money + v_allowance - v_leave_fine);

    -- Thu nhập chịu thuế (Taxable) = Gross - Tiền ăn (Miễn thuế)
    SET v_taxable_income = GREATEST(0, v_gross_income - v_meal_amount);

    -- Thu nhập tính thuế (Sau giảm trừ)
    SET v_tax_calc_basis = GREATEST(0, v_taxable_income - v_total_insurance - (11000000 + v_dependents * 4400000));

    -- Biểu thuế lũy tiến TNCN (Cập nhật đầy đủ 7 bậc)
    IF v_tax_calc_basis > 0 THEN
        IF v_tax_calc_basis <= 5000000 THEN 
            SET v_tax_amount = v_tax_calc_basis * 0.05;
        ELSEIF v_tax_calc_basis <= 10000000 THEN 
            SET v_tax_amount = (v_tax_calc_basis * 0.1) - 250000;
        ELSEIF v_tax_calc_basis <= 18000000 THEN 
            SET v_tax_amount = (v_tax_calc_basis * 0.15) - 750000;
        ELSEIF v_tax_calc_basis <= 32000000 THEN 
            SET v_tax_amount = (v_tax_calc_basis * 0.2) - 1650000;
        ELSEIF v_tax_calc_basis <= 52000000 THEN 
            SET v_tax_amount = (v_tax_calc_basis * 0.25) - 3250000;
        -- Thêm nấc 6: Từ trên 52tr đến 80tr
        ELSEIF v_tax_calc_basis <= 80000000 THEN 
            SET v_tax_amount = (v_tax_calc_basis * 0.3) - 5850000;
        -- Thêm nấc 7: Trên 80tr
        ELSE 
            SET v_tax_amount = (v_tax_calc_basis * 0.35) - 9850000;
        END IF;
    END IF;

    -- NET = Gross - Bảo hiểm - Thuế - Phạt vi phạm
    SET v_net_salary = GREATEST(0, v_gross_income - v_total_insurance - v_tax_amount - v_violation);

    -- 7. LƯU DỮ LIỆU
    INSERT INTO payroll_history (
        employee_id, salary_period, base_salary, standard_work_days, actual_work_days,
        bhxh_amount, bhyt_amount, bhtn_amount, total_insurance,
        violation_amount, total_allowance, overtime_amount,
        taxable_income, tax_amount, net_salary
    )
    VALUES (
        p_employee_id, p_salary_period, v_base_salary, v_standard_days, v_actual_days,
        v_bhxh, v_bhyt, v_bhtn, v_total_insurance,
        (v_violation + v_leave_fine), v_allowance, v_ot_money,
        v_taxable_income, v_tax_amount, v_net_salary
    )
    ON DUPLICATE KEY UPDATE
        base_salary = VALUES(base_salary),
        standard_work_days = VALUES(standard_work_days),
        actual_work_days = VALUES(actual_work_days),
        bhxh_amount = VALUES(bhxh_amount),
        bhyt_amount = VALUES(bhyt_amount),
        bhtn_amount = VALUES(bhtn_amount),
        total_insurance = VALUES(total_insurance),
        violation_amount = VALUES(violation_amount),
        total_allowance = VALUES(total_allowance),
        overtime_amount = VALUES(overtime_amount),
        taxable_income = VALUES(taxable_income),
        tax_amount = VALUES(tax_amount),
        net_salary = VALUES(net_salary);

END$$

DROP PROCEDURE IF EXISTS sp_RunAllPayroll;

-- PROCEDURE CHẠY CHO TẤT CẢ NHÂN VIÊN
CREATE PROCEDURE sp_RunAllPayroll()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE emp_id INT;
    DECLARE cur_period DATE;
    DECLARE cur CURSOR FOR SELECT id FROM employee;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Lấy ngày đầu tháng của tháng trước
    SET cur_period = DATE_SUB(DATE_FORMAT(NOW(), '%Y-%m-01'), INTERVAL 1 MONTH);

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO emp_id;
        IF done THEN LEAVE read_loop; END IF;
        CALL sp_CalculateMonthlyPayroll(emp_id, cur_period);
    END LOOP;
    CLOSE cur;
END$$

-- EVENT TỰ ĐỘNG NGÀY 5 HÀNG THÁNG
DROP EVENT IF EXISTS evt_MonthlyPayrollAuto$$
CREATE EVENT evt_MonthlyPayrollAuto
ON SCHEDULE EVERY 1 MONTH
STARTS DATE_FORMAT(NOW() + INTERVAL 1 MONTH, '%Y-%m-05 08:00:00')
DO
  CALL sp_RunAllPayroll()$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_SyncEmploymentChanges;
CREATE PROCEDURE sp_SyncEmploymentChanges()
BEGIN
    DECLARE v_status_approved INT;
    DECLARE v_status_effective INT;

    -- Lấy ID trạng thái dựa trên Name và Type để tránh hard-code con số
    SELECT id INTO v_status_approved 
    FROM status 
    WHERE name = 'Approved' AND type = 'EMPLOYMENT_HISTORY' LIMIT 1;
    
    SELECT id INTO v_status_effective 
    FROM status 
    WHERE name = 'Effective' AND type = 'EMPLOYMENT_HISTORY' LIMIT 1;

    -- Chỉ chạy nếu tìm thấy ID trạng thái hợp lệ
    IF v_status_approved IS NOT NULL AND v_status_effective IS NOT NULL THEN
        
        -- 1. Cập nhật Position và Phòng ban cho Employee
       UPDATE employee e
		INNER JOIN employment_history h ON e.id = h.employee_id
		SET 
			-- Nếu department_id trong history khác hiện tại thì mới update, không thì giữ nguyên
			e.department_id = h.department_id,
			-- Tương tự cho position_id
			e.position_id = h.position_id,
			e.updated_at = NOW()
		WHERE h.status_id = v_status_approved 
		  AND h.effective_date <= CURDATE()
		  -- Thêm điều kiện: chỉ update nếu có ít nhất 1 trong 2 cái thay đổi so với bảng chính
		  AND (e.department_id != h.department_id OR e.position_id != h.position_id);

        -- 3. Chuyển trạng thái lịch sử sang 'Effective'
        UPDATE employment_history 
        SET status_id = v_status_effective 
        WHERE status_id = v_status_approved 
          AND effective_date <= CURDATE();
          
    END IF;

END$$

DELIMITER ;

DELIMITER $$

DROP EVENT IF EXISTS evt_DailyEmploymentSync;
CREATE EVENT evt_DailyEmploymentSync
ON SCHEDULE EVERY 1 DAY -- Quét mỗi ngày một lần
STARTS CURRENT_TIMESTAMP -- Bắt đầu ngay bây giờ
DO
BEGIN
    CALL sp_SyncEmploymentChanges();
END$$

DELIMITER ;
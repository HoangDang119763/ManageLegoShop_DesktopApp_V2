#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script tạo phiếu bán hàng (invoices) cho tháng 1 và 2 năm 2026
- Từ 5/1 đến 29/2
- Mỗi ngày ít nhất 1 phiếu
- Sử dụng hàng từ import 3 (ngày 2026-01-01)
- Output: INSERT statements
"""

import random
from datetime import datetime, timedelta
from decimal import Decimal

# ============================================
# PRODUCT DATA FROM IMPORT 3 (2026-01-01)
# ============================================

products_import_3 = {
    'SP00001': {'qty_available': 40, 'import_price': 15000, 'selling_price': 22500},
    'SP00002': {'qty_available': 40, 'import_price': 15000, 'selling_price': 22500},
    'SP00003': {'qty_available': 40, 'import_price': 15000, 'selling_price': 22500},
    'SP00004': {'qty_available': 40, 'import_price': 12000, 'selling_price': 18000},
    'SP00005': {'qty_available': 40, 'import_price': 12000, 'selling_price': 17400},
    'SP00006': {'qty_available': 40, 'import_price': 12000, 'selling_price': 17400},
    'SP00007': {'qty_available': 40, 'import_price': 15000, 'selling_price': 21750},
    'SP00008': {'qty_available': 50, 'import_price': 15000, 'selling_price': 22500},
    'SP00009': {'qty_available': 3, 'import_price': 1000000, 'selling_price': 1500000},
    'SP00010': {'qty_available': 3, 'import_price': 850000, 'selling_price': 1275000},
    'SP00011': {'qty_available': 3, 'import_price': 850000, 'selling_price': 1275000},
    'SP00012': {'qty_available': 3, 'import_price': 850000, 'selling_price': 1275000},
    'SP00013': {'qty_available': 3, 'import_price': 890000, 'selling_price': 1335000},
    'SP00014': {'qty_available': 3, 'import_price': 750000, 'selling_price': 1125000},
}

# ============================================
# GENERATE INVOICE DATA
# ============================================

def generate_invoices():
    """
    Generate invoice data for Jan 5 - Feb 29, 2026
    - Mỗi ngày ít nhất 1 phiếu
    - Random products from import 3
    
    Returns: (invoices, detail_invoices, final_inventory)
    """
    
    invoices = []
    detail_invoices = []
    
    # Sales staff IDs
    sales_staff = [7, 8, 9, 10, 11, 12]
    customers_list = list(range(1, 29))
    
    # Current inventory (mutable)
    current_inventory = {**products_import_3}
    
    # Store original qty for statistics
    original_inventory = {}
    for prod_id, data in current_inventory.items():
        original_inventory[prod_id] = data['qty_available']
    
    current_invoice_id = 75  # Continue from previous invoice IDs
    
    # Date range: Jan 5 - Feb 29, 2026
    start_date = datetime(2026, 1, 5)
    end_date = datetime(2026, 2, 28)
    
    current_date = start_date
    
    while current_date <= end_date:
        # 1-3 invoices per day (mostly 1-2)
        num_invoices_today = random.choice([1, 1, 1, 2, 2, 3])
        
        for inv_idx in range(num_invoices_today):
            # Random time in the day (8:00 to 18:00)
            hour = random.randint(8, 17)
            minute = random.randint(0, 59)
            second = random.randint(0, 59)
            invoice_datetime = current_date.replace(hour=hour, minute=minute, second=second)
            
            # Random staff and customer
            employee_id = random.choice(sales_staff)
            customer_id = random.choice(customers_list)
            
            # Filter products with stock
            available_with_stock = [
                p for p in products_import_3.keys() 
                if current_inventory[p]['qty_available'] > 0
            ]
            
            if not available_with_stock:
                # Skip if no products available
                continue
            
            # Random 1-3 products per invoice
            num_products = random.randint(1, min(3, len(available_with_stock)))
            selected_products = random.sample(available_with_stock, num_products)
            
            total_price = Decimal('0.00')
            has_items = False
            
            # Create detail records
            for product_id in selected_products:
                # Random quantity
                if current_inventory[product_id]['qty_available'] > 10:
                    qty = random.randint(1, 3)
                else:
                    qty = random.randint(1, min(2, current_inventory[product_id]['qty_available']))
                
                # Make sure we don't exceed available stock
                qty = min(qty, current_inventory[product_id]['qty_available'])
                
                if qty <= 0:
                    continue
                
                price = current_inventory[product_id]['selling_price']
                cost_price = current_inventory[product_id]['import_price']
                line_total = Decimal(price) * qty
                
                detail_invoices.append({
                    'invoice_id': current_invoice_id,
                    'product_id': product_id,
                    'quantity': qty,
                    'price': price,
                    'cost_price': cost_price,
                    'total_price': line_total,
                })
                
                total_price += line_total
                current_inventory[product_id]['qty_available'] -= qty
                has_items = True
            
            # Only create invoice if it has items
            if has_items:
                invoices.append({
                    'invoice_id': current_invoice_id,
                    'created_at': invoice_datetime.strftime('%Y-%m-%d %H:%M:%S'),
                    'employee_id': employee_id,
                    'customer_id': customer_id,
                    'discount_code': None,
                    'discount_amount': Decimal('0.00'),
                    'total_price': total_price,
                    'status_id': 15  # COMPLETED
                })
                current_invoice_id += 1
        
        # Move to next day
        current_date += timedelta(days=1)
    
    return invoices, detail_invoices, original_inventory, current_inventory

# ============================================
# GENERATE SQL
# ============================================

def generate_sql(invoices, detail_invoices, final_inventory):
    """Generate SQL insert statements with product stock updates"""
    
    sql_lines = []
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- Auto-Generated Invoice Data for Jan 5 - Feb 29, 2026")
    sql_lines.append("-- Using Import 3 products (2026-01-01)")
    sql_lines.append("-- ===============================================")
    sql_lines.append("")
    
    # Update product stock_quantity
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- UPDATE PRODUCT STOCK AFTER SALES")
    sql_lines.append("-- ===============================================")
    for prod_id in sorted(final_inventory.keys()):
        final_qty = final_inventory[prod_id]['qty_available']
        sql_lines.append(f"UPDATE product SET stock_quantity = {final_qty} WHERE id = '{prod_id}';")
    sql_lines.append("")
    
    # Insert invoices
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- INSERT INVOICES")
    sql_lines.append("-- ===============================================")
    sql_lines.append("LOCK TABLES `invoice` WRITE;")
    sql_lines.append("/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;")
    sql_lines.append("INSERT INTO `invoice` (`id`, `created_at`, `employee_id`, `customer_id`, `discount_code`, `discount_amount`, `total_price`, `status_id`) VALUES")
    
    invoice_values = []
    for inv in invoices:
        value = f"({inv['invoice_id']}, '{inv['created_at']}', {inv['employee_id']}, {inv['customer_id']}, NULL, {inv['discount_amount']}, {inv['total_price']:.2f}, {inv['status_id']})"
        invoice_values.append(value)
    
    sql_lines.append(",\n".join(invoice_values) + ";")
    sql_lines.append("/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;")
    sql_lines.append("UNLOCK TABLES;")
    sql_lines.append("")
    
    # Insert detail invoices
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- INSERT DETAIL INVOICES")
    sql_lines.append("-- ===============================================")
    sql_lines.append("LOCK TABLES `detail_invoice` WRITE;")
    sql_lines.append("/*!40000 ALTER TABLE `detail_invoice` DISABLE KEYS */;")
    sql_lines.append("INSERT INTO `detail_invoice` (`invoice_id`, `product_id`, `quantity`, `price`, `cost_price`, `total_price`) VALUES")
    
    detail_values = []
    for detail in detail_invoices:
        value = f"({detail['invoice_id']}, '{detail['product_id']}', {detail['quantity']}, {detail['price']:.2f}, {detail['cost_price']:.2f}, {detail['total_price']:.2f})"
        detail_values.append(value)
    
    sql_lines.append(",\n".join(detail_values) + ";")
    sql_lines.append("/*!40000 ALTER TABLE `detail_invoice` ENABLE KEYS */;")
    sql_lines.append("UNLOCK TABLES;")
    sql_lines.append("")
    
    # Summary
    sql_lines.append("-- ===============================================")
    sql_lines.append(f"-- SUMMARY:")
    sql_lines.append(f"-- Total Invoices: {len(invoices)}")
    sql_lines.append(f"-- Total Detail Rows: {len(detail_invoices)}")
    sql_lines.append(f"-- Period: 2026-01-05 to 2026-02-28")
    sql_lines.append(f"-- Days: {(datetime(2026, 2, 28) - datetime(2026, 1, 5)).days + 1}")
    sql_lines.append("-- ===============================================")
    
    return "\n".join(sql_lines)

# ============================================
# MAIN
# ============================================

if __name__ == "__main__":
    random.seed(42)  # For reproducibility
    
    print("Generating invoices for Jan 5 - Feb 29, 2026...")
    invoices, detail_invoices, original_inventory, final_inventory = generate_invoices()
    
    print(f"✓ Generated {len(invoices)} invoices")
    print(f"✓ Generated {len(detail_invoices)} detail rows")
    
    # Generate SQL
    sql = generate_sql(invoices, detail_invoices, final_inventory)
    
    # Write to file
    output_file = "generated_invoices_jan_feb.sql"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(sql)
    
    print(f"✓ SQL saved to: {output_file}")
    
    # Print statistics
    print("\n=== STATISTICS ===")
    print(f"Total Invoices: {len(invoices)}")
    print(f"Date Range: 2026-01-05 to 2026-02-28")
    print(f"Days: {(datetime(2026, 2, 28) - datetime(2026, 1, 5)).days + 1}")
    print(f"Average invoices per day: {len(invoices) / 56:.2f}")
    
    total_revenue = sum(inv['total_price'] for inv in invoices)
    print(f"Total Revenue: {total_revenue:,.0f} VND")
    
    # Product sold summary
    print("\n=== PRODUCTS SOLD ===")
    product_sales = {}
    for detail in detail_invoices:
        prod_id = detail['product_id']
        if prod_id not in product_sales:
            product_sales[prod_id] = {'qty': 0, 'revenue': 0}
        product_sales[prod_id]['qty'] += detail['quantity']
        product_sales[prod_id]['revenue'] += float(detail['total_price'])
    
    for prod_id in sorted(final_inventory.keys()):
        original_qty = original_inventory[prod_id]
        final_qty = final_inventory[prod_id]['qty_available']
        sold_qty = original_qty - final_qty
        revenue = product_sales.get(prod_id, {}).get('revenue', 0)
        if sold_qty > 0:
            print(f"  {prod_id}: Sold {sold_qty}/{original_qty}, Remaining {final_qty}, Revenue {revenue:,.0f} VND")

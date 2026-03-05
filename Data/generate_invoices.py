#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script tạo phiếu bán hàng (invoices) phù hợp với 2 lô nhập
- Tháng 12/2025 (01-31)
- Mỗi ngày ít nhất 2 phiếu
- Không dùng giảm giá
- Dàn trải hết sản phẩm từ 2 lô nhập
"""

import random
from datetime import datetime, timedelta
from decimal import Decimal

# ============================================
# DATA FROM IMPORTS
# ============================================

# Import 1: 2025-12-01, SP00001-SP00007 (30 each)
# Import 2: 2025-12-15, SP00008-SP00014 (2 each)

products_import_1 = {
    'SP00001': {'qty_available': 30, 'import_price': 15000, 'selling_price': 22500},
    'SP00002': {'qty_available': 30, 'import_price': 15000, 'selling_price': 22500},
    'SP00003': {'qty_available': 30, 'import_price': 15000, 'selling_price': 22500},
    'SP00004': {'qty_available': 30, 'import_price': 12000, 'selling_price': 18000},
    'SP00005': {'qty_available': 30, 'import_price': 12000, 'selling_price': 17400},
    'SP00006': {'qty_available': 30, 'import_price': 12000, 'selling_price': 17400},
    'SP00007': {'qty_available': 30, 'import_price': 15000, 'selling_price': 21750},
}

products_import_2 = {
    'SP00008': {'qty_available': 30, 'import_price': 15000, 'selling_price': 22500},
    'SP00009': {'qty_available': 2, 'import_price': 1000000, 'selling_price': 1500000},
    'SP00010': {'qty_available': 2, 'import_price': 850000, 'selling_price': 1275000},
    'SP00011': {'qty_available': 2, 'import_price': 850000, 'selling_price': 1275000},
    'SP00012': {'qty_available': 2, 'import_price': 850000, 'selling_price': 1275000},
    'SP00013': {'qty_available': 2, 'import_price': 890000, 'selling_price': 1335000},
    'SP00014': {'qty_available': 2, 'import_price': 750000, 'selling_price': 1125000},
}

# Merge products
all_products = {**products_import_1, **products_import_2}

# ============================================
# FUNCTION TO GENERATE INVOICE DATA
# ============================================

def generate_invoices():
    """
    Generate invoice data for December 2025
    - 01-14/12: Chỉ bán SP00001-SP00007 (từ import 1 ngày 2025-12-01)
    - 15-31/12: Bán cả SP00001-SP00014 (import 2 vào 2025-12-15)
    - Mỗi ngày ít nhất 2 phiếu
    - Random products, staff, customers
    
    Returns: (invoices, detail_invoices, final_inventory)
    """
    
    invoices = []
    detail_invoices = []
    invoice_id = 1
    
    # Track original qty for statistics
    original_inventory = {}
    
    # Sales staff IDs
    sales_staff = [7, 8, 9, 10, 11, 12]
    customers_list = list(range(1, 29))
    
    # Invoices per day: 2-3
    invoices_per_day = [2, 2, 2, 3]  # More variety
    
    # Current inventory (mutable) - initialize with all products
    # But SP00008-SP00014 start with qty_available = 0 until day 15
    current_inventory = {**products_import_1}
    for prod_id in products_import_2:
        current_inventory[prod_id] = {
            'qty_available': 0,  # Start at 0, will be updated on day 15
            'import_price': products_import_2[prod_id]['import_price'],
            'selling_price': products_import_2[prod_id]['selling_price']
        }
    
    # Store original qty for statistics
    for prod_id, data in current_inventory.items():
        original_inventory[prod_id] = data['qty_available']
    
    detail_id = 1
    current_invoice_id = 1
    
    for day in range(1, 32):
        # On day 15, add products from import 2
        if day == 15:
            for prod_id in products_import_2:
                current_inventory[prod_id]['qty_available'] = products_import_2[prod_id]['qty_available']
        
        # Determine which products are available
        if day < 15:
            available_products = {**products_import_1}  # SP00001-SP00007
        else:
            # From 15/12 onwards, add new products
            available_products = {**products_import_1, **products_import_2}  # All products
        
        # Random time for the day (8:00 to 18:00)
        hour = random.randint(8, 17)
        minute = random.randint(0, 59)
        second = random.randint(0, 59)
        invoice_date = datetime(2025, 12, day, hour, minute, second)
        
        # Random 2-3 invoices per day
        num_invoices_today = random.choice(invoices_per_day)
        
        for _ in range(num_invoices_today):
            # Random time offset (5-60 minutes apart)
            time_offset = timedelta(minutes=random.randint(5, 60))
            invoice_date = invoice_date + time_offset
            
            # Random staff
            employee_id = random.choice(sales_staff)
            
            # Random customer
            customer_id = random.choice(customers_list)
            
            # Filter products with available stock
            available_with_stock = [
                p for p in available_products.keys() 
                if current_inventory[p]['qty_available'] > 0
            ]
            
            if not available_with_stock:
                continue  # Skip if no products available
            
            # Random 1-3 products per invoice
            num_products = random.randint(1, min(3, len(available_with_stock)))
            selected_products = random.sample(available_with_stock, num_products)
            
            total_price = Decimal('0.00')
            has_items = False
            
            # Create detail records
            for product_id in selected_products:
                # Random quantity (1-3 for minifigures, 1 for models)
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
                    'detail_id': detail_id
                })
                
                total_price += line_total
                current_inventory[product_id]['qty_available'] -= qty
                detail_id += 1
                has_items = True
            
            # Only create invoice if it has items
            if has_items:
                invoices.append({
                    'invoice_id': current_invoice_id,
                    'created_at': invoice_date.strftime('%Y-%m-%d %H:%M:%S'),
                    'employee_id': employee_id,
                    'customer_id': customer_id,
                    'discount_code': None,
                    'discount_amount': Decimal('0.00'),
                    'total_price': total_price,
                    'status_id': 15  # COMPLETED
                })
                current_invoice_id += 1
    
    return invoices, detail_invoices, original_inventory, current_inventory

# ============================================
# GENERATE SQL
# ============================================

def generate_sql(invoices, detail_invoices, final_inventory):
    """Generate SQL insert statements with product stock updates"""
    
    sql_lines = []
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- Auto-Generated Invoice Data for December 2025")
    sql_lines.append("-- Timeline:")
    sql_lines.append("--   01-14/12: SP00001-SP00007 (from import 1)")
    sql_lines.append("--   15-31/12: SP00001-SP00014 (all products)")
    sql_lines.append("-- ===============================================")
    sql_lines.append("USE java_sql;")
    sql_lines.append("")
    
    # Update product stock_quantity
    sql_lines.append("-- ===============================================")
    sql_lines.append("-- UPDATE PRODUCT STOCK AFTER SALES")
    sql_lines.append("-- ===============================================")
    for prod_id, inv_data in sorted(final_inventory.items()):
        final_qty = inv_data['qty_available']
        sql_lines.append(f"UPDATE product SET stock_quantity = {final_qty} WHERE id = '{prod_id}';")
    sql_lines.append("")
    
    # Insert invoices
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
    sql_lines.append(f"-- Period: 01-14/12 (SP01-07) + 15-31/12 (SP01-14)")
    sql_lines.append("-- ===============================================")
    
    return "\n".join(sql_lines)

# ============================================
# MAIN
# ============================================

if __name__ == "__main__":
    random.seed(42)  # For reproducibility (change or remove for different data)
    
    print("Generating invoices...")
    invoices, detail_invoices, original_inventory, final_inventory = generate_invoices()
    
    print(f"✓ Generated {len(invoices)} invoices")
    print(f"✓ Generated {len(detail_invoices)} detail rows")
    
    # Generate SQL
    sql = generate_sql(invoices, detail_invoices, final_inventory)
    
    # Write to file
    output_file = "generated_invoices.sql"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(sql)
    
    print(f"✓ SQL saved to: {output_file}")
    
    # Print statistics
    print("\n=== STATISTICS ===")
    print(f"Total Invoices: {len(invoices)}")
    print(f"Date Range: 2025-12-01 to 2025-12-31")
    print(f"  - 01-14/12 (14 days): SP00001-SP00007 only")
    print(f"  - 15-31/12 (17 days): SP00001-SP00014 (after import 2)")
    print(f"Average invoices per day: {len(invoices) / 31:.1f}")
    
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
    
    # Use final inventory (actual after sales) to show remaining stocks
    for prod_id in sorted(final_inventory.keys()):
        original_qty = original_inventory[prod_id]
        final_qty = final_inventory[prod_id]['qty_available']
        sold_qty = original_qty - final_qty
        revenue = product_sales.get(prod_id, {}).get('revenue', 0)
        print(f"  {prod_id}: Sold {sold_qty}/{original_qty}, Remaining {final_qty}, Revenue {revenue:,.0f} VND")

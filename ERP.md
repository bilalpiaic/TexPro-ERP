# ERP.md - TexPro Textile ERP & Accounting System

## 1. Introduction
This document details the architecture and operational workflows for **TexPro ERP**, a complete enterprise textile accounting system built for manufacturing and converter businesses. The system coordinates the complete lot-wise sequence—from initial customer Sale Order, through purchasing Grey Cloth (in lots by Quality/Blend/Width) from weaving mills, issuing to dyeing & printing processors, transferring to CMT stitchers, to final customer delivery and billing.

## 2. Categorized General Vouchers
The system strictly implements the 7 standard categorized financial vouchers:
1. **JV (Journal Voucher):** Non-cash double-entry adjustments, inventory WIP movements, processor/stitcher cost capitalization, and period-end accruals.
2. **CR (Cash Receipt Voucher):** Cash received into mill/petty cash box (e.g., cash sales of cutting waste or customer cash receipts).
3. **CP (Cash Payment Voucher):** Cash disbursements for carriage, mill loading, driver bilty, or local factory labor.
4. **BP (Bank Payment Voucher):** Cheque or online bank transfers to Grey Weaving Mills, Dye/Print Processors, and Stitching Units.
5. **BR (Bank Receipt Voucher):** Inward bank transfers, wires, or LC collections from buyers for finished goods sale invoices.
6. **Sale (Sale Voucher / Invoice):** Billing customers for finished goods (bed sets, garments, fabric rolls) with direct Cost of Goods Sold (COGS) derecognition.
7. **Purchase (Purchase Voucher / Bill):** Inward Grey Cloth billing from weaving mills or Job Work processing invoices from finishing mills.

## 3. Lot-Wise Manufacturing Sequence & Quantitative Flow
Every batch is tracked **Lot-Wise** and backed by a **Customer Sale Order (SO)** from start till end:
- **Phase 0 - Sale Order (SO):** Customer order booking with target pieces, estimated meters, quality (e.g., 40x40/100x80), blend (e.g., 100% Combed Cotton), width (e.g., 105"), unit price, and delivery timeline.
- **Phase 1 - Grey Cloth Purchase (Purchase Voucher):**
  - Purchasing raw fabric in lots from Weaving Mills.
  - Quantitative: Grey Meters, Fabric Width, Quality, Blend, Thans/Rolls.
  - Accounting Impact: `Dr 1410 Inventory - Grey Cloth`, `Cr 2010 Accounts Payable - Grey Mills`.
- **Phase 2 - Processing (Dyeing & Printing) (JV & PV):**
  - Issuance to processor: `Dr 1420 WIP - Processing`, `Cr 1410 Inventory - Grey Cloth`.
  - Finished fabric receipt: Quantitative output in meters with shrinkage/loss tracking.
  - Job work fee: `Dr 1430 Inventory - Finished Processed Fabric`, `Cr 1420 WIP - Processing`, `Cr 2020 AP - Processors`.
- **Phase 3 - Stitching & CMT (Cut, Make, Trim) (JV):**
  - Issuance to stitching contractor for cutting and packaging into finished goods.
  - Quantitative: Converts fabric meters into finished units (e.g., 4,940m -> 1,200 Bed Sets).
  - CMT fee: `Dr 1440 Inventory - Finished Goods`, `Cr 1430 Finished Fabric`, `Cr 2030 AP - Stitchers`.
- **Phase 4 - Customer Dispatch & Invoicing (Sale Voucher):**
  - Dispatching finished units to the customer fulfilling the linked Sale Order.
  - Quantitative: Finished units shipped, unit selling price, revenue.
  - Accounting Impact:
    - `Dr 1200 Accounts Receivable`, `Cr 4010 Sales Revenue`.
    - `Dr 5010 COGS Grey`, `Dr 5020 COGS Processing`, `Dr 5030 COGS Stitching`, `Cr 1440 Finished Goods Inventory`.
    - Computes real-time gross profit and margin per lot!

## 4. General Ledger & Financial Reporting
- **General Journal:** Chronological audit trail of all 7 voucher categories with quantitative tags.
- **General Ledger:** Account-level running balance ledger with debit, credit, and quantity details.
- **Trial Balance:** Multi-column trial balance showing verified zero-variance double-entry balancing.
- **Profit & Loss:** Sales Revenue, Cost of Goods Sold (Grey, Dyeing, Stitching), Gross Profit, Operating Overheads, Net Operating Profit.
- **Balance Sheet:** Assets (Cash, Bank, Grey, WIP, Finished Goods, AR) vs. Liabilities (AP Mills, Processors, Stitchers) + Equity.
- **Lot Profitability Analysis:** Full quantitative meters/units reconciliation and margin breakdown per lot.

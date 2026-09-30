# ssd.md - Structure Driven Development Plan (Spec-Kit Inspired)

## 1. Introduction
This document details the Structure Driven Development (SDD) approach for LedgerPro ERP, incorporating principles from GitHub Spec-Kit: specification-first contracts, automated testing, formalized schemas, and iterative sprints.

## 2. SDD Phases & Deliverables

### 2.1 /constitution (Vision & Principles)
- **Goal:** Establish immutable foundations and invariants.
- **Rules:** Double-entry ledger rule ($\sum \text{Dr} == \sum \text{Cr}$), modularity, GAAP compliance.

### 2.2 /specify (API Specification & Data Modeling)
- **Goal:** Formal OpenAPI 3.0 contracts before runtime implementation.
- **Endpoints:**
  - `GET /accounts`: List Chart of Accounts
  - `POST /accounts`: Create new account with classification
  - `POST /journal-entries`: Post balanced double-entry voucher
  - `GET /reports/balance-sheet`: Generate live balance sheet
  - `POST /stocks/trade`: Execute stock transaction & sync ledger

### 2.3 /review (Stakeholder Validation & Audit)
- **Goal:** Architectural compliance scorecard and continuous verification.
- **Scorecard:** Double-Entry Invariant (Passed), 5-Class COA Structure (Compliant), AR/AP Ledger Hook (Verified), DCF Modeling (Verified).

### 2.4 /plan (Sprint Roadmap)
- **Sprint 1:** Architecture & OpenAPI Spec (100%)
- **Sprint 2:** Core Accounting, COA & Double-Entry Ledger (100%)
- **Sprint 3:** Invoicing, Vendor Bills & Auto-Bookkeeping (100%)
- **Sprint 4:** Stock Holdings & DCF Valuation Simulation (100%)
- **Sprint 5:** SDD Spec Workbench & Interactive OAS Runner (100%)
- **Sprint 6:** AI Continuous Auditor & Anomaly Detection (100%)

### 2.5 /tasks (Sprint Backlog & Kanban)
- Active sprint tasks with status tracking (Todo, In Progress, Done), assignee, story points, and priority badges.

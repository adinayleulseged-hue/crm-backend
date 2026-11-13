# 📢 Campaign Management System

## Overview

The Campaign Management module allows Marketing Managers to create, launch, and track marketing campaigns to generate leads and drive sales.

---

## 🎯 Campaign Use Cases

### **UC80: View All Campaigns**
**Actor:** Marketing Manager, Admin  
**Description:** View list of all marketing campaigns with status and performance metrics

**Preconditions:**
- User is authenticated
- User has permission to view campaigns

**Main Flow:**
1. User navigates to Campaigns dashboard
2. System displays list of campaigns with:
   - Campaign name and ID
   - Status (Draft, Active, Paused, Completed)
   - Start/End dates
   - Budget allocated
   - Leads generated
   - Conversion rate
3. User can filter by status, date range, or campaign type

**Postconditions:**
- Campaign list is displayed with current metrics

---

### **UC81: Create Campaign**
**Actor:** Marketing Manager, Admin  
**Description:** Create a new marketing campaign

**Preconditions:**
- User is authenticated
- User has CREATE_CAMPAIGN permission

**Main Flow:**
1. User clicks "Create Campaign"
2. System displays campaign creation form
3. User enters:
   - Campaign name
   - Campaign type (Email, Event, Webinar, Product Launch)
   - Description
   - Campaign goal (Lead Generation, Brand Awareness, Sales)
4. System validates input
5. **<<include>>** Define Target Audience (UC86)
6. **<<extend>>** Set Campaign Budget (UC87) - Optional
7. System creates campaign with status "DRAFT"
8. System generates unique campaign ID
9. System saves campaign to database

**Postconditions:**
- New campaign created with Draft status
- Campaign ID assigned
- Audit log entry created

**Extensions:**
- **UC86 (Define Target Audience)** - REQUIRED
  - Select customer segments
  - Define demographic filters
  - Import contact lists
- **UC87 (Set Budget)** - OPTIONAL
  - Enter budget amount
  - Set spending limits
  - Track ROI

---

### **UC83: Launch Campaign**
**Actor:** Marketing Manager, Admin  
**Description:** Activate a draft campaign and begin execution

**Preconditions:**
- Campaign exists in DRAFT or PAUSED status
- Campaign has target audience defined
- Campaign content is complete

**Main Flow:**
1. User selects campaign to launch
2. User clicks "Launch Campaign"
3. System validates campaign is ready:
   - Has target audience
   - Has email content (if email campaign)
   - Has valid dates
4. **<<include>>** Send Bulk Email (UC91) - for email campaigns
5. **<<extend>>** Schedule Campaign (UC88) - if scheduled for future
6. System updates campaign status to "ACTIVE"
7. System starts tracking campaign metrics
8. System sends confirmation to Marketing Manager

**Postconditions:**
- Campaign status changed to ACTIVE
- Emails sent to target audience (if email campaign)
- Tracking started for all metrics
- Notification sent to campaign creator

**Extensions:**
- **UC88 (Schedule Campaign)** - OPTIONAL
  - Set launch date/time
  - Set end date/time
  - Configure automation rules

---

### **UC84: Pause Campaign**
**Actor:** Marketing Manager, Admin  
**Description:** Temporarily halt an active campaign

**Preconditions:**
- Campaign is ACTIVE
- User has EDIT_CAMPAIGN permission

**Main Flow:**
1. User selects active campaign
2. User clicks "Pause Campaign"
3. System prompts for confirmation
4. User confirms
5. System updates status to "PAUSED"
6. System stops all automated actions
7. System maintains current metrics
8. System logs pause action with timestamp and user

**Postconditions:**
- Campaign status changed to PAUSED
- No further automated actions taken
- Metrics frozen at current values
- Can be resumed later

---

### **UC85: Close Campaign**
**Actor:** Marketing Manager, Admin  
**Description:** Mark campaign as completed and generate final report

**Preconditions:**
- Campaign is ACTIVE or PAUSED
- User has CLOSE_CAMPAIGN permission

**Main Flow:**
1. User selects campaign to close
2. User clicks "Close Campaign"
3. System prompts for campaign summary/notes
4. User enters closing notes
5. System updates status to "COMPLETED"
6. System calculates final metrics:
   - Total leads generated
   - Conversion rate
   - Total revenue attributed
   - ROI (if budget was set)
7. System generates final campaign report
8. System archives campaign data
9. System notifies stakeholders

**Postconditions:**
- Campaign status changed to COMPLETED
- Final report generated
- Campaign archived
- Metrics finalized

---

### **UC86: Define Target Audience** (<<include>> in Create Campaign)
**Actor:** Marketing Manager  
**Description:** Select and filter target audience for campaign

**Main Flow:**
1. User opens audience definition interface
2. User selects targeting criteria:
   - Industry (Banking, Government, Healthcare, etc.)
   - Company size (Small, Medium, Enterprise)
   - Geographic location
   - Lead source (Website, Referral, Event)
   - Lead status (NEW, CONTACTED, QUALIFIED)
3. System displays estimated audience size
4. User can upload custom contact list (CSV)
5. System validates contacts
6. User saves audience definition
7. System associates audience with campaign

**Postconditions:**
- Target audience defined
- Estimated reach calculated
- Audience saved with campaign

---

### **UC89: Track Campaign Performance**
**Actor:** Marketing Manager, Admin  
**Description:** Monitor real-time campaign metrics and KPIs

**Main Flow:**
1. User navigates to campaign dashboard
2. User selects campaign to track
3. System displays real-time metrics:
   - **Emails Sent:** Total emails delivered
   - **Open Rate:** % of emails opened
   - **Click Rate:** % of links clicked
   - **Leads Generated:** New leads from campaign
   - **Qualified Leads:** Leads that were qualified
   - **Opportunities Created:** Sales opportunities generated
   - **Revenue Attributed:** Total revenue from campaign leads
   - **Cost per Lead:** Budget / Leads generated
   - **ROI:** (Revenue - Budget) / Budget
4. System displays charts and graphs
5. User can export metrics to CSV/PDF

**Postconditions:**
- Performance metrics displayed
- User understands campaign effectiveness

---

### **UC90: Generate Campaign Report**
**Actor:** Marketing Manager, Admin  
**Description:** Create comprehensive campaign performance report

**Main Flow:**
1. User selects campaign
2. User clicks "Generate Report"
3. System collects data:
   - Campaign details (name, dates, budget)
   - Audience statistics
   - Email metrics (if applicable)
   - Lead generation statistics
   - Conversion funnel data
   - Revenue attribution
   - ROI calculations
4. System generates report in selected format (PDF, Excel)
5. User can customize report sections
6. System saves report
7. User downloads report

**Postconditions:**
- Campaign report generated
- Report available for download
- Report saved in system

---

### **UC91: Send Bulk Email** (<<include>> in Launch Campaign)
**Actor:** Email System  
**Description:** Send campaign emails to target audience

**Main Flow:**
1. System retrieves target audience list
2. System validates all email addresses
3. For each contact:
   - System personalizes email content
   - System sends email via SMTP
   - System logs email sent timestamp
4. **<<extend>>** Track Email Opens (UC92)
5. **<<extend>>** Track Click Rates (UC93)
6. System updates campaign metrics
7. System handles bounces and failures

**Postconditions:**
- Emails sent to all valid contacts
- Send statistics recorded
- Tracking pixels embedded (if enabled)

**Extensions:**
- **UC92 (Track Opens)** - OPTIONAL
  - Embed tracking pixel in email
  - Record when email is opened
  - Track open timestamp and user agent
- **UC93 (Track Clicks)** - OPTIONAL
  - Replace links with tracking URLs
  - Record when links are clicked
  - Track which links were clicked

---

### **UC94: Assign Leads to Campaign**
**Actor:** Marketing Manager, Admin  
**Description:** Associate existing leads with a campaign for tracking

**Main Flow:**
1. User opens campaign
2. User clicks "Assign Leads"
3. System displays available leads
4. User selects leads to assign
5. System validates lead assignment
6. System updates lead records with campaign ID
7. System updates campaign lead count
8. System logs assignment

**Postconditions:**
- Leads assigned to campaign
- Campaign metrics updated
- Lead attribution tracked

---

## 📊 Campaign Types

### **1. Email Marketing Campaign**
- Send promotional emails to target audience
- Track opens, clicks, conversions
- A/B testing support
- Automated follow-ups

### **2. Lead Generation Campaign**
- Drive traffic to landing pages
- Capture lead information
- Qualify leads automatically
- Nurture leads through funnel

### **3. Product Launch Campaign**
- Announce new products/services
- Generate pre-orders
- Build anticipation
- Track early adopters

### **4. Event Marketing Campaign**
- Promote webinars, conferences, workshops
- Manage registrations
- Send reminders
- Follow up with attendees

### **5. Nurture Campaign**
- Automated email sequences
- Educational content delivery
- Lead scoring
- Gradual conversion

---

## 🎯 Campaign Metrics (KPIs)

### **Email Metrics:**
- **Sent:** Total emails delivered
- **Open Rate:** (Opens / Sent) × 100
- **Click Rate:** (Clicks / Sent) × 100
- **Bounce Rate:** (Bounces / Sent) × 100
- **Unsubscribe Rate:** (Unsubscribes / Sent) × 100

### **Lead Metrics:**
- **Leads Generated:** New leads from campaign
- **Lead Quality Score:** Average lead score
- **Qualified Leads:** Leads that passed qualification
- **Conversion Rate:** (Qualified / Generated) × 100

### **Sales Metrics:**
- **Opportunities Created:** Sales deals from campaign leads
- **Opportunities Won:** Closed deals from campaign
- **Revenue Attributed:** Total revenue from campaign
- **Average Deal Size:** Revenue / Opportunities Won

### **ROI Metrics:**
- **Cost per Lead:** Budget / Leads Generated
- **Cost per Acquisition:** Budget / Customers Acquired
- **ROI:** ((Revenue - Budget) / Budget) × 100
- **Payback Period:** Months to recover campaign cost

---

## 🔄 Campaign Lifecycle

```
DRAFT → ACTIVE → PAUSED → ACTIVE → COMPLETED
  ↓                          ↓
  └────────────→ CANCELLED ←┘
```

### **Status Definitions:**

- **DRAFT:** Campaign created but not launched
- **ACTIVE:** Campaign running and sending communications
- **PAUSED:** Temporarily halted, can be resumed
- **COMPLETED:** Campaign ended, final report generated
- **CANCELLED:** Campaign stopped permanently

---

## 🎨 Campaign Dashboard (UI Mockup)

```
┌──────────────────────────────────────────────────────┐
│  📢 Campaign Management                    [+ New]   │
├──────────────────────────────────────────────────────┤
│                                                       │
│  Active Campaigns (3)                                │
│  ┌────────────────────────────────────────────────┐ │
│  │ 🎯 Q1 2026 Lead Gen Campaign        🟢 ACTIVE  │ │
│  │ Leads: 127 | Open Rate: 32% | ROI: +245%      │ │
│  │ [Pause] [View Details] [Report]                │ │
│  └────────────────────────────────────────────────┘ │
│                                                       │
│  ┌────────────────────────────────────────────────┐ │
│  │ 📧 Product Launch Email             🟢 ACTIVE  │ │
│  │ Sent: 1,543 | Clicks: 487 | Conv: 18%         │ │
│  │ [Pause] [View Details] [Report]                │ │
│  └────────────────────────────────────────────────┘ │
│                                                       │
│  Draft Campaigns (2)                                 │
│  ┌────────────────────────────────────────────────┐ │
│  │ ✏️  Summer Webinar Series           ⚪ DRAFT   │ │
│  │ [Edit] [Launch] [Delete]                       │ │
│  └────────────────────────────────────────────────┘ │
│                                                       │
│  Completed Campaigns (15)         [View Archive →]  │
│                                                       │
└──────────────────────────────────────────────────────┘
```

---

## 🔗 Integration with Other Modules

### **Lead Management:**
- Campaigns generate new leads
- Leads are automatically assigned campaign ID
- Lead source tracked as "CAMPAIGN"
- Lead scoring influenced by campaign engagement

### **Sales Management:**
- Campaign leads can be converted to opportunities
- Campaign attribution tracked on deals
- Revenue linked back to originating campaign
- Sales team can see lead's campaign history

### **Email System:**
- All campaign emails logged
- Delivery status tracked
- Bounces and failures recorded
- Email templates managed centrally

### **Analytics & Reporting:**
- Campaign metrics feed into reports
- ROI calculations automated
- Cross-campaign comparisons
- Trend analysis over time

---

## ✅ Implementation Checklist

- [ ] Create Campaign entity in database
- [ ] Add campaign_id foreign key to Lead table
- [ ] Implement CampaignService with CRUD operations
- [ ] Create CampaignController with REST endpoints
- [ ] Build campaign dashboard UI
- [ ] Implement email tracking (opens/clicks)
- [ ] Add campaign metrics calculation
- [ ] Create campaign report generator
- [ ] Implement target audience filters
- [ ] Add campaign ROI calculator
- [ ] Create campaign archive functionality
- [ ] Add permissions for campaign management
- [ ] Test campaign lifecycle flows

---

## 🚀 Future Enhancements

- A/B testing for email content
- AI-powered audience recommendations
- Automated lead scoring from campaign engagement
- Multi-channel campaigns (email + SMS + social)
- Campaign templates library
- Predictive analytics for campaign performance
- Integration with social media platforms
- Real-time campaign optimization suggestions

---

**Campaign Management adds powerful marketing automation to your CRM!** 📢

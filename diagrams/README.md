# 📊 INSA CRM Diagrams

## Available Diagrams

### **1. Use Case Diagram** ✅
**File:** `INSA_CRM_UseCase.puml`

**What it shows:**
- All system actors (8 roles)
- All use cases (90+ use cases)
- **<<include>>** relationships (mandatory dependencies)
- **<<extend>>** relationships (optional features)
- Complete role permissions

**Key Features:**
- ✅ Public visitor use cases
- ✅ Customer portal
- ✅ Lead management with qualification
- ✅ **Campaign management** (NEW!)
- ✅ Sales deal approval workflow
- ✅ Financial operations
- ✅ User administration
- ✅ Audit & compliance

**Actors:**
1. Public Visitor
2. Customer
3. Sales Officer
4. Sales Manager
5. **Marketing Manager** (NEW!)
6. Finance Officer
7. Auditor
8. Admin

---

## 🎯 Campaign Management Highlights

### **New Use Cases Added:**

**UC80-UC94: Campaign Management Package**
- UC80: View All Campaigns
- UC81: Create Campaign (<<include>> Define Audience)
- UC82: Edit Campaign
- UC83: Launch Campaign (<<include>> Send Bulk Email)
- UC84: Pause Campaign
- UC85: Close Campaign
- UC86: Define Target Audience
- UC87: Set Campaign Budget (<<extend>>)
- UC88: Schedule Campaign (<<extend>>)
- UC89: Track Campaign Performance
- UC90: Generate Campaign Report
- UC91: Send Bulk Email (<<include>>)
- UC92: Track Email Opens (<<extend>>)
- UC93: Track Click Rates (<<extend>>)
- UC94: Assign Leads to Campaign

### **Campaign Types Supported:**
1. 📧 Email Marketing
2. 🎯 Lead Generation
3. 🚀 Product Launch
4. 🎪 Event Marketing
5. 🌱 Nurture Campaign

### **Campaign Metrics Tracked:**
- Email: Open Rate, Click Rate, Bounce Rate
- Leads: Generated, Qualified, Conversion Rate
- Sales: Opportunities, Revenue, Deal Size
- ROI: Cost per Lead, Cost per Acquisition, Payback Period

---

## 📋 <<include>> vs <<extend>> Summary

### **<<include>> = MANDATORY** (Always Happens)
✅ Login includes Validate Credentials  
✅ Create User includes Generate Password  
✅ Invite Customer includes Send Email  
✅ Qualify Lead includes Send Notification  
✅ Launch Campaign includes Send Bulk Email  
✅ Create Campaign includes Define Audience  

### **<<extend>> = OPTIONAL** (May Happen)
➕ Qualification extends with Optional Reason  
➕ Approval extends with Optional Discount  
➕ Payment extends with Optional Receipt  
➕ Campaign extends with Optional Budget Setting  
➕ Campaign extends with Optional Scheduling  
➕ Email extends with Optional Tracking  

---

## 🚀 How to Render

### **Quick Online (No Installation):**
1. Go to: http://www.plantuml.com/plantuml/uml/
2. Copy content from `INSA_CRM_UseCase.puml`
3. Paste and view!

### **VS Code (Recommended):**
1. Install "PlantUML" extension
2. Open `.puml` file
3. Press `Alt + D` to preview
4. Export as PNG/SVG/PDF

### **Command Line:**
```powershell
java -jar plantuml.jar INSA_CRM_UseCase.puml
```

**See:** `HOW_TO_RENDER_DIAGRAM.md` for complete instructions

---

## 📁 File Structure

```
crm-backend/diagrams/
├── INSA_CRM_UseCase.puml          ← Main diagram file
├── HOW_TO_RENDER_DIAGRAM.md       ← Rendering instructions
├── README.md                       ← This file
└── (Generated outputs)
    ├── INSA_CRM_UseCase.png       ← Generated image
    ├── INSA_CRM_UseCase.svg       ← Scalable vector
    └── INSA_CRM_UseCase.pdf       ← Printable version
```

---

## 📖 Related Documentation

- **Campaign Details:** `../CAMPAIGN_MANAGEMENT.md`
- **Use Case Details:** `../USE_CASE_DIAGRAM.md`
- **Role Permissions:** `../../crm-frontend/insa-erp-crm/ROLE_ACCESS_CONTROL.md`

---

## 🎨 Diagram Statistics

- **Total Actors:** 8 (including Email System)
- **Total Use Cases:** 94
- **Total Packages:** 9
- **<<include>> Relationships:** 15+
- **<<extend>> Relationships:** 12+
- **Colors Used:** 9 (for visual organization)

---

## 🔄 Diagram Updates

### **Version 1.0 - Initial Release**
- Basic use cases for all modules
- Authentication flow
- Lead management
- Sales and finance operations

### **Version 2.0 - Campaign Management** ✨ NEW!
- Added Marketing Manager actor
- Added Campaign Management package (15 use cases)
- Added email tracking capabilities
- Added campaign lifecycle
- Enhanced with campaign metrics

---

## ✅ Validation Checklist

- [x] All actors defined with clear roles
- [x] All use cases have unique IDs
- [x] All <<include>> relationships are mandatory
- [x] All <<extend>> relationships are optional
- [x] Actor permissions match system design
- [x] Diagram follows UML best practices
- [x] Color coding for visual clarity
- [x] Notes added for complex use cases
- [x] Campaign management fully integrated
- [x] Email system as external actor

---

## 🎓 Learning Resources

- **PlantUML Syntax:** https://plantuml.com/use-case-diagram
- **UML Best Practices:** https://www.uml-diagrams.org/use-case-diagrams.html
- **Include vs Extend:** https://stackoverflow.com/questions/1696927/

---

## 💡 Tips for Reading the Diagram

1. **Start with Actors** - Understand who uses the system
2. **Follow the flows** - See what each actor can do
3. **Notice <<include>>** - These always happen together
4. **Notice <<extend>>** - These are optional additions
5. **Check colors** - Each package has a theme color
6. **Read notes** - Important details are annotated

---

## 🚀 Next Steps

1. **Render the diagram** using any method above
2. **Review with team** for completeness
3. **Create sequence diagrams** for complex flows
4. **Design class diagram** for entities
5. **Create activity diagrams** for business processes

---

**Your complete use case diagram is ready!** 🎉

Generate it now and share with your team!

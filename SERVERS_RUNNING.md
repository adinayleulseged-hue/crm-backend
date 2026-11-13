# ✅ Servers Status - ALL RUNNING!

## 🚀 Services Currently Running

### **✅ Frontend (Next.js)**
- **Status:** 🟢 RUNNING
- **Port:** 3000
- **URL:** http://localhost:3000
- **Network URL:** http://10.96.2.219:3000
- **Access:** Open in browser → http://localhost:3000

### **✅ Backend (Spring Boot)**
- **Status:** 🟢 RUNNING  
- **Port:** 8082
- **API URL:** http://localhost:8082
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **API Docs:** http://localhost:8082/v3/api-docs

### **✅ Database (PostgreSQL)**
- **Status:** 🟢 RUNNING
- **Port:** 5433
- **Database:** crm_db
- **Username:** postgres
- **Connection:** localhost:5433/crm_db

---

## 🌐 Access Your Application

### **1. Open Frontend:**
```
http://localhost:3000
```

### **2. Login Credentials:**

**Admin User:**
```
Username: admin
Password: admin123
```

**Customer User:**
```
Username: customer
Password: customer123
```

**Sales Manager:**
```
Username: salesmanager
Password: manager123
```

**Finance Officer:**
```
Username: finance
Password: finance123
```

---

## 🧪 Test API Endpoints

### **Test Backend Health:**
```bash
# Open in browser:
http://localhost:8082/swagger-ui.html
```

### **Test Invoice Rejection (Finance Officer):**
```bash
curl -X PATCH "http://localhost:8082/api/invoices/1/reject?reason=Incorrect%20VAT" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **Get Pending Invoices:**
```bash
curl "http://localhost:8082/api/invoices/pending-approval" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **Approve Deal (Sales Manager):**
```bash
curl -X PATCH "http://localhost:8082/api/opportunities/1/approve?discountApproved=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📋 Complete Workflow Test

### **Step-by-Step Test:**

1. **Open Frontend:** http://localhost:3000

2. **Login as Customer:**
   - Go to Customer Portal
   - Create a new deal
   - Submit for approval

3. **Login as Sales Manager:**
   - Go to Sales Manager Dashboard
   - See pending deal
   - Approve or reject

4. **Login as Customer Again:**
   - See approval notification
   - Place order
   - Invoice auto-generated

5. **Login as Finance Officer:**
   - Go to Finance Dashboard  
   - See pending invoice
   - Approve or reject

6. **Login as Customer Final:**
   - See "PAID" status if approved
   - See delivery tracking

---

## 🔧 Stop/Restart Services

### **Stop All Services:**
```powershell
# Stop frontend (Ctrl+C in terminal)
# Stop backend (Ctrl+C in terminal)
```

### **Restart Frontend:**
```bash
cd crm-frontend/insa-erp-crm
npm run dev
```

### **Restart Backend:**
```bash
cd crm-backend
mvn spring-boot:run
```

---

## 📊 Server Logs

### **View Frontend Logs:**
Check the terminal where `npm run dev` is running

### **View Backend Logs:**
Check the terminal where `mvn spring-boot:run` is running

### **Note:** 
- RabbitMQ connection errors are normal (RabbitMQ is optional)
- Backend is fully functional without RabbitMQ

---

## ✅ Everything You Need to Know

### **What's Working:**
- ✅ Frontend accessible at http://localhost:3000
- ✅ Backend API accessible at http://localhost:8082
- ✅ Database migrations applied (V15 included)
- ✅ All workflow endpoints operational
- ✅ Email notifications configured
- ✅ Complete deal → payment workflow ready

### **What to Do Next:**
1. Open http://localhost:3000 in your browser
2. Login with credentials above
3. Test the workflow
4. Check the documentation files for integration details

---

## 🎉 Your System is Ready!

**Both frontend and backend are running successfully!**

You can now:
- Access the application at http://localhost:3000
- Test API endpoints at http://localhost:8082/swagger-ui.html
- Implement the remaining frontend features using the provided documentation

**All systems operational!** 🚀

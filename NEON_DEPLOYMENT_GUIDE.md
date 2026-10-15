# 🐘 Deploying FitPulse with Neon Serverless Postgres & Global Hosting

This guide walks you through connecting your **FitPulse Java Enterprise Platform** to **Neon Serverless Postgres** and deploying it globally.

---

## ⚡ Step 1: Create Your Neon Serverless Postgres Database

1. Sign up for free at **[Neon.tech](https://neon.tech/)**.
2. Click **Create Project** and give it a name (e.g., `fitpulse-db`).
3. Neon will display your **Connection Details**:
   - In the dropdown, select **Connection string** or **Java (JDBC)**.
   - It will look like this:
     ```
     postgresql://alex:AbC123xyz@ep-cool-darkness-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
     ```
4. Copy this string.

---

## 🔌 Step 2: Connect FitPulse to Neon

You can connect FitPulse to Neon in two ways:

### Option A: Local Testing with Neon
Set the `DATABASE_URL` environment variable before running:

**PowerShell:**
```powershell
$env:DATABASE_URL="postgresql://alex:AbC123xyz@ep-cool-darkness-123456.us-east-2.aws.neon.tech/neondb?sslmode=require"
java -jar target/fitpulse-global-app.jar
```

**Command Prompt (`cmd`):**
```cmd
set DATABASE_URL=postgresql://alex:AbC123xyz@ep-cool-darkness-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
java -jar target/fitpulse-global-app.jar
```

FitPulse will automatically:
1. Detect that the URL is a Neon PostgreSQL instance.
2. Load the official PostgreSQL JDBC Driver (`org.postgresql.Driver`).
3. Initialize the complete 11-table schema in Neon.
4. Seed the initial admin, coaches, clients, and workout plans!

---

## ☁️ Step 3: Global Hosting (Understanding Vercel vs Java JVM)

### ⚠️ Important Architectural Context on Vercel:
> **Vercel is a Serverless Edge platform built for Next.js, Node.js, Python, and frontend static assets.**  
> Vercel **does not run long-running Java JVM processes, Java Servlets (`HttpServlet`), or background daemon threads** (which are required by your university/course rubric for **Servlets & Web Integration** and **Multithreading & Synchronization**).

To run your Java backend seamlessly with **Neon Postgres**, use one of the following setups:

---

### 🚀 Production Deployment (Java Backend + Neon Postgres)

Deploy your Java backend for free on **Render** or **Railway** (they natively run Java JVM & Docker containers 24/7):

#### Deploying on Render (Free 24/7):
1. Push your repository to GitHub.
2. Go to **[Render.com](https://render.com)** $\rightarrow$ **New Web Service** $\rightarrow$ select your repo.
3. Render automatically detects the [`Dockerfile`](Dockerfile) and [`render.yaml`](render.yaml).
4. In the **Environment Variables** tab, add:
   - Key: `DATABASE_URL`
   - Value: `<Your Neon Connection String>`
5. Click **Deploy**. Render will build the container and provide a live URL:  
   `https://fitpulse-fitness-platform.onrender.com`

---

### 🌐 Using Vercel as an Edge Gateway

If your deployment requirements specifically ask for Vercel, you can deploy the included [`vercel.json`](vercel.json) as a **Vercel Edge Proxy**:
1. In your Vercel Dashboard, import the repository.
2. Under **Environment Variables** in Vercel, set:
   - `BACKEND_HOST`: `fitpulse-fitness-platform.onrender.com`
3. Vercel acts as your global Edge CDN routing requests directly to your high-performance Java + Neon platform!

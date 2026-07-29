# Bank Application - Local Kubernetes Deployment Guide

A Spring Boot 3.4.5 banking application (Java 21) with Thymeleaf UI and MySQL database. This guide covers step-by-step deployment on local Kubernetes clusters.

## Table of Contents

- [Application Overview](#application-overview)
- [Prerequisites](#prerequisites)
- [Step 1: Build Docker Image](#step-1-build-docker-image)
- [Step 2: Start Local Kubernetes](#step-2-start-local-kubernetes)
- [Step 3: Deploy with Helm](#step-3-deploy-with-helm)
- [Step 4: Access the Application](#step-4-access-the-application)
- [Step 5: Verify Deployment](#step-5-verify-deployment)
- [Step 6: Troubleshooting](#step-6-troubleshooting)
- [Useful Commands](#useful-commands)
- [Application Features](#application-features)

---

## Application Overview

**Bank Application** is a Spring Boot 3.4.5 web application that manages banking operations:
- Account management (create, update, view accounts)
- Deposit and withdrawal operations
- Money transfer between accounts
- Thymeleaf-based web interface

**Technology Stack:**
- Spring Boot 3.4.5
- Java 21
- MySQL 8.0
- Thymeleaf (Template Engine)
- Spring Data JPA
- Maven

---

## Prerequisites

Install the following tools on your local machine:

### 1. **Java Development Kit (JDK) 21**
```bash
java -version  # Should show version 21.x.x
```

### 2. **Apache Maven 3.8+**
```bash
mvn --version  # Should show version 3.8.x or higher
```

### 3. **Docker**
```bash
docker --version  # Should show version 20.10.x or higher
```

### 4. **Kubernetes CLI (kubectl)**
```bash
kubectl version --client  # Should show version 1.24+
```

### 5. **Helm 3.x**
```bash
helm version  # Should show version v3.x
```

### 6. **Local Kubernetes**

Docker Desktop has Kubernetes built-in. Just enable it in settings.

---

## Step 1: Build Docker Image

### 1.1 Build the Application JAR

Navigate to the project root and build:

```bash
cd "d:\Bank App\Bank-Application"
mvn clean package -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXs
```

### 1.2 Build the Docker Image

```bash
# Build with your Docker username
docker build -t <docker_user_name>/bank-app:latest .
```

**Dockerfile details:**
- Base image: `eclipse-temurin:21-jre`
- Port: 8080
- JAR file: `/app/app.jar`

### 1.3 Push Image to Docker Hub

```bash
# Login to Docker Hub (first time only)
docker login

# Push the image
docker push <docker_user_name>/bank-app:latest

# Verify push
docker inspect <docker_user_name>/bank-app:latest
```

### 1.4 Verify the Image

```bash
docker images | grep bank-app
```

---

## Step 2: Start Local Kubernetes

### Docker Desktop Setup (Recommended)

```bash
# Enable Kubernetes in Docker Desktop Settings
# Settings > Kubernetes > Enable Kubernetes

# Verify cluster
kubectl cluster-info
kubectl get nodes
```

### Create Namespace

```bash
# Create namespace
kubectl create namespace bank-app

# Verify
kubectl get namespaces

# Set as default (optional)
kubectl config set-context --current --namespace=bank-app
```

---

## Step 3: Deploy with Helm

### 3.1 Verify Helm Chart

```bash
cd helm/Bank-App

# Lint the chart
helm lint .
```

### 3.2 Update values.yaml

Edit `helm/Bank-App/values.yaml`:

```yaml
replicaCount: 1  # Single replica for local development

image:
  repository: <docker_user_name>/bank-app    # Your Docker username/image name
  tag: latest                                # Use latest tag
  pullPolicy: IfNotPresent                   # Don't pull from registry

service:
  type: ClusterIP    # Use port-forward for access
  port: 80
  targetPort: 8080

mysql:
  database: SBI
  username: root
  password: "7953"
  port: 3306
  storage: 1Gi       # Minimal storage for local
```

### 3.3 Install Bank Application with MySQL

```bash
# Install Helm chart (includes both MySQL and Bank App)
helm install bank-app ./helm/Bank-App --namespace bank-app
```

Verify installation:
```bash
helm list -n bank-app
helm status bank-app -n bank-app
```

Wait for MySQL and application to be ready:
```bash
# Wait for MySQL
kubectl wait --for=condition=ready pod -l app=mysql -n bank-app --timeout=300s

# Wait for application
kubectl wait --for=condition=available --timeout=300s \
  deployment/bank-app-app -n bank-app
```

Verify all pods are running:
```bash
kubectl get pods -n bank-app
kubectl logs -n bank-app -l app=mysql
kubectl logs -n bank-app -l app=bank-app
```

---

## Step 4: Access the Application

### Quick Access with Port Forward

```bash
# Forward local port to service
kubectl port-forward -n bank-app svc/bank-app-service 8080:80
```

Access in browser: **http://localhost:8080**

The application shows:
- Accounts list
- Create account
- Deposit/Withdraw/Transfer money
- Update account details

---

## Step 5: Verify Deployment

### 5.1 Check All Resources

```bash
# View all pods
kubectl get pods -n bank-app

# View all services
kubectl get svc -n bank-app

# View all deployments
kubectl get deployment -n bank-app
```

Expected output:
```
NAME                        READY   STATUS    RESTARTS   AGE
pod/bank-app-app-xxxxx      1/1     Running   0          2m
pod/mysql-0                 1/1     Running   0          3m

NAME                 TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)
service/bank-app-app   ClusterIP   10.x.x.x      <none>        80/TCP
service/mysql          ClusterIP   10.x.x.x      <none>        3306/TCP
```

### 5.2 Check Logs

```bash
# Application logs
kubectl logs -n bank-app -l app=bank-app

# Follow logs in real-time
kubectl logs -n bank-app -l app=bank-app -f

# MySQL logs
kubectl logs -n bank-app -l app=mysql
```

### 5.3 Verify Database Connection

```bash
# Enter application pod
kubectl exec -it -n bank-app deployment/bank-app-app -- sh

# Inside the pod, check if it can reach MySQL
wget -O- http://localhost:8080 | head -20
```

---

## Step 6: Troubleshooting

### Pod Not Running

```bash
# Check pod status
kubectl describe pod <pod-name> -n bank-app

# Common issues:
# - ImagePullBackOff: Image not found (use local tag without registry)
# - Pending: Insufficient resources (close other applications)
```

### Application Can't Connect to MySQL

```bash
# Verify MySQL pod is running
kubectl get pods -n bank-app | grep mysql

# Check MySQL logs
kubectl logs -n bank-app -l app=mysql

# Verify service
kubectl get svc -n bank-app | grep mysql

# Describe service
kubectl describe svc mysql -n bank-app
```

### Port Forward Not Working

```bash
# Kill existing port-forward
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows PowerShell

# Start fresh
kubectl port-forward -n bank-app svc/bank-app-service 8080:80
```

### Application Crashes Immediately

```bash
# Check logs
kubectl logs -n bank-app <pod-name> --previous

# Common causes:
# 1. Wrong database credentials
# 2. MySQL not ready when app starts
# 3. Environment variables not set

# Verify environment variables
kubectl get deployment bank-app-app -n bank-app -o yaml | grep -A 10 "env:"
```

---

## Useful Commands

### View Everything

```bash
# All resources
kubectl get all -n bank-app

# Detailed view
kubectl get all -n bank-app -o wide
```

### Scale Application

```bash
# Scale to 3 replicas
kubectl scale deployment bank-app-app --replicas=3 -n bank-app

# Verify
kubectl get pods -n bank-app
```

### Update Application

After code changes:

```bash
# 1. Rebuild Docker image
mvn clean package -DskipTests
docker build -t <docker_user_name>/bank-app:v2 .

# 2. Update deployment
kubectl set image deployment/bank-app-app \
  bank-app=<docker_user_name>/bank-app:v2 -n bank-app

# 3. Watch rollout
kubectl rollout status deployment/bank-app-app -n bank-app -w
```

### View Pod Details

```bash
# Describe pod
kubectl describe pod <pod-name> -n bank-app

# Get pod YAML
kubectl get pod <pod-name> -n bank-app -o yaml

# Execute command
kubectl exec -it <pod-name> -n bank-app -- sh
```

### Resource Usage

```bash
# View resource usage
kubectl top pods -n bank-app
kubectl top nodes
```

### Restart Application

```bash
# Delete pod (new one will auto-start)
kubectl delete pod -l app=bank-app -n bank-app

# Or scale down then up
kubectl scale deployment bank-app-app --replicas=0 -n bank-app
kubectl scale deployment bank-app-app --replicas=1 -n bank-app
```

### View Events

```bash
# Recent events
kubectl get events -n bank-app --sort-by='.lastTimestamp'

# Watch events
kubectl get events -n bank-app -w
```

### Clean Up Everything

```bash
# Delete Helm release
helm uninstall bank-app -n bank-app

# Delete namespace (removes all resources)
kubectl delete namespace bank-app
```

---

## Complete Quick Start

```bash
# 1. Build image
mvn clean package -DskipTests
docker build -t <docker_user_name>/bank-app:latest .

# 2. Push to Docker Hub
docker login
docker push <docker_user_name>/bank-app:latest

# 3. Create namespace
kubectl create namespace bank-app

# 4. Deploy application (includes MySQL)
helm install bank-app ./helm/Bank-App --namespace bank-app

# 5. Wait for deployments to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n bank-app --timeout=300s
kubectl wait --for=condition=available --timeout=300s deployment/bank-app-app -n bank-app

# 6. Access application
kubectl port-forward -n bank-app svc/bank-app-service 8080:80
# Visit: http://localhost:8080
```

---

## Application Features

### Accounts List
![Screenshot 2025-05-27 103358](https://github.com/user-attachments/assets/6465cb23-ffb4-4ac7-9814-b51d02a8402a)

### Create Account
![Screenshot 2025-05-27 103407](https://github.com/user-attachments/assets/766a3804-4ded-43ef-9b37-cc70b1cf39a1)

### Withdraw Money
![Screenshot 2025-05-27 103425](https://github.com/user-attachments/assets/826c8c12-8102-4f2f-b0e7-fc207eb88980)

### Transfer Money
![Screenshot 2025-05-27 103445](https://github.com/user-attachments/assets/e4cc149c-d8a9-488c-a273-bcd1734ce285)

### Update Account Details
![Screenshot 2025-05-27 103458](https://github.com/user-attachments/assets/e4cc149c-d8a9-488c-a273-bcd1734ce285)

---

## Support & Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

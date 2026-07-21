# Deploy Lab — Jenkins + Docker + Kubernetes

Projekt treningowy dla Java Developera. Pozwala przećwiczyć pełny pipeline:

1. pobranie kodu z repozytorium,
2. build aplikacji Mavenem,
3. uruchomienie testów,
4. budowę obrazu Docker,
5. wdrożenie aplikacji do lokalnego Kubernetes,
6. rollout i smoke test.

## Wymagania

- Windows 10/11,
- Git,
- Docker Desktop,
- w Docker Desktop włączony Kubernetes,
- konto GitHub lub inne repozytorium Git.

Sprawdź:

```powershell
docker version
kubectl cluster-info
git --version
```

## 1. Uruchomienie aplikacji bez Jenkinsa

```powershell
mvn clean package
docker build -t deploy-lab:latest .
docker run --rm -p 8080:8080 deploy-lab:latest
```

Otwórz:

- http://localhost:8080
- http://localhost:8080/api/hello
- http://localhost:8080/actuator/health

## 2. Ręczny deploy do Kubernetes

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl -n deploy-lab get all
kubectl -n deploy-lab port-forward service/deploy-lab 8080:8080
```

W drugim terminalu:

```powershell
curl http://localhost:8080
```

Usunięcie środowiska:

```powershell
kubectl delete namespace deploy-lab
```

## 3. Założenie repozytorium

W katalogu projektu:

```powershell
git init
git add .
git commit -m "Initial Jenkins Kubernetes deployment lab"
git branch -M main
git remote add origin ADRES_TWOJEGO_REPOZYTORIUM
git push -u origin main
```

## 4. Uruchomienie Jenkinsa

Najpierw upewnij się, że plik istnieje:

```powershell
Test-Path $env:USERPROFILE\.kube\config
```

Uruchom:

```powershell
docker compose up -d --build
```

Jenkins będzie dostępny pod:

- http://localhost:8081

Hasło startowe:

```powershell
docker exec deploy-lab-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 5. Utworzenie pipeline w Jenkins

1. Kliknij **New Item**.
2. Nazwa: `deploy-lab`.
3. Wybierz **Pipeline**.
4. W sekcji Pipeline wybierz **Pipeline script from SCM**.
5. SCM: **Git**.
6. Wpisz URL repozytorium.
7. Branch: `*/main`.
8. Script Path: `Jenkinsfile`.
9. Zapisz i kliknij **Build Now**.

## 6. Co robi Jenkinsfile

Pipeline ma etapy:

- Checkout,
- Build and test,
- Build Docker image,
- Deploy to Kubernetes,
- Smoke test.

Każdy build tworzy obraz:

```text
deploy-lab:<numer_builda>
```

Następnie aktualizuje Deployment i czeka na zakończenie rollout.

## 7. Najważniejsze komendy diagnostyczne

```powershell
kubectl -n deploy-lab get pods
kubectl -n deploy-lab get deployment
kubectl -n deploy-lab describe pod NAZWA_PODA
kubectl -n deploy-lab logs NAZWA_PODA
kubectl -n deploy-lab rollout history deployment/deploy-lab
kubectl -n deploy-lab rollout status deployment/deploy-lab
```

Rollback:

```powershell
kubectl -n deploy-lab rollout undo deployment/deploy-lab
```

Skalowanie:

```powershell
kubectl -n deploy-lab scale deployment/deploy-lab --replicas=4
```

## 8. Ćwiczenia

### Ćwiczenie 1 — zwykły deployment

Zmień tekst w `DeploymentController`, zrób commit i push. Uruchom pipeline ponownie. Sprawdź numer wersji i nazwę poda w odpowiedzi endpointu `/`.

### Ćwiczenie 2 — zepsuty test

Zmień test tak, aby nie przechodził. Pipeline powinien zatrzymać się przed budową obrazu.

### Ćwiczenie 3 — zepsuty readiness probe

W `k8s/deployment.yaml` zmień ścieżkę readiness probe na nieistniejącą. Obserwuj timeout rollout i użyj `kubectl describe pod`.

### Ćwiczenie 4 — rollback

Wdróż działającą wersję, potem wersję zepsutą i wykonaj rollback.

### Ćwiczenie 5 — skalowanie

Zwiększ liczbę replik z 2 do 4 i sprawdź, czy kolejne wywołania endpointu pokazują różne nazwy podów.

### Ćwiczenie 6 — ConfigMap i Secret

Dodaj ConfigMap z nazwą środowiska i Secret z przykładowym tokenem. Wstrzyknij je jako zmienne środowiskowe.

### Ćwiczenie 7 — osobne środowiska

Dodaj katalogi:

```text
k8s/dev
k8s/sit
k8s/prod
```

Następnie przygotuj osobne namespace i parametry replik.

## Uwaga dotycząca Docker Desktop

Pipeline zakłada, że Jenkins uruchomiony w kontenerze korzysta z Docker Engine hosta oraz z konfiguracji Kubernetes znajdującej się w `%USERPROFILE%\.kube`.

Gdy `kubectl` wewnątrz Jenkinsa nie może połączyć się z klastrem, sprawdź zawartość pliku `.kube/config`. Dla Docker Desktop adres serwera zwykle powinien być osiągalny jako `https://kubernetes.docker.internal:6443` albo przez `host.docker.internal`.

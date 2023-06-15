# Chaos Mesh tests for Kubernetes Client SharedInformer

This project is an example on how to run Chaos tests for SharedInformers.

### Setup

Start minikube:

```bash
minikube stop && minikube delete && minikube start --driver=docker --memory 8192 --cpus 3
```

Install ChaosMesh on minikube:

```bash
curl -sSL https://mirrors.chaos-mesh.org/v2.6.0/install.sh | bash
```

Wait for the pods to be all ready:

```bash
kubectl wait --for=condition=Ready pods -n chaos-mesh --all --timeout=600s
```

### Test

The sequence of operations that will run during each test:

- delete the target ConfigMap
- run the checker pod
- run the control pod
- check the status of the pods

### Glossary

- checker: run the SharedInformer to get notifications over the shared resource
- control: apply timely changes to the shared resource

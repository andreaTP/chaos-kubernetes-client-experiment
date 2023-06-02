
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

Going to use One-time Chaos experiment with defined "duration"



To test:

- delete the target ConfigMap
- run the probe command (possibly in a pod)
- run the control command (possibly in a pod)

- check the status of the pods!


TODO: The two control/checker apps can live in the same maven project

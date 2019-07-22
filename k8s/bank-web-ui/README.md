## K8s setup

At present, we use [kops](https://github.com/kubernetes/kops) to [configure the cluster](https://owlabs.atlassian.net/wiki/spaces/DTY/pages/133534866/Kubernetes). But, sadly, there's a few other things we need to setup:

### EFS

For persistent storage, some services use Amazon's [EFS](https://aws.amazon.com/efs/) via the [aws-efs](https://github.com/kubernetes-incubator/external-storage/tree/master/aws/efs) provisioner. We only run in one region, so we configured [the filesystem](https://eu-central-1.console.aws.amazon.com/efs/home?region=eu-central-1#/filesystems/fs-5bc82402) to be presented in `eu-central-1a` (the only az where we have the VPC subnets) on the [private k8s subnet](https://eu-central-1.console.aws.amazon.com/vpc/home?region=eu-central-1#subnets:) (named `eu-central-1a.enki.k8s.local` in this case).

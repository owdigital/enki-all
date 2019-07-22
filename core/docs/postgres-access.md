# Accessing PostgreSQL on k8s

At the moment, we have three instances of postgres deployed. We have the `postgres` deployment, which is associated with `enki-core` application and hydra instance, and then we have `pg-bank-a` and `pg-bank-b` which are used by the banks' hydra instances.

The easiest way to access the `psql` shell is via `kubectl`'s `exec` subcommand. Eg:

```
: kubectl -n develop get pods
NAME                             READY     STATUS    RESTARTS   AGE
agent-3697362937-mz1rx           1/1       Running   0          53m
bank-hydra-3279569724-jvkk8      1/1       Running   0          55m
enki-b-web-ui-1711644293-vlfb4   1/1       Running   0          19m
core-2092058388-lb0b1            1/1       Running   0          3m
hydra-2463286528-t1xdg           1/1       Running   0          3m
pg-bank-1034664137-8c8lj         1/1       Running   0          1h
postgres-3897152666-6c5jp        1/1       Running   0          54m
: kubectl --namespace=develop exec -t -i postgres-3897152666-6c5jp -- psql -U postgres enki-core
psql (9.6.5)
Type "help" for help.

core=# \d
                     List of relations
 Schema |             Name              | Type  |  Owner
--------+-------------------------------+-------+----------
 public | bank                          | table | postgres
 public | metadata_assertion            | table | postgres
 public | metadata_assertion_revocation | table | postgres
 public | pii_type                      | table | postgres
 public | schema_version                | table | postgres
 public | share_assertion               | table | postgres
 public | share_assertion_revocation    | table | postgres
 public | sharing_purpose               | table | postgres
 public | user                          | table | postgres
 public | user_association              | table | postgres
(10 rows)

core=#
```

ie: List out the running pods in the develop (staging) namespace, pick out the one starting `postgres-`, and then pass that to `kubectl exec`.

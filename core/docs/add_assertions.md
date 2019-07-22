## Adding dummy metadata and share assertions

Run `(load-file "test-assertions.clj")`. This creates:

1) 2 banks - `"Bank A"` and `"Bank B"`
2) 2 metadata assertions (`firstName`, `lastName`) and 2 share assertions for Bank A.
3) 1 share assertions for Bank B.
4) User - `alicia`, password - `123`.

It also links the user with the bank users for Bank A and Bank B.

The Bank A/B users have a password `fixme!` for enki-core login.

## Manual linking of the bank and enki accounts

There are 2 routes for demonstrating the linking of the enki and bank accounts,
depending on if the registration of user _alicia_ to bank-A actually feeds
Consus and Enki or not.

In any case you need to register bank-ui-A on Enki, which is currently done by
applying the `test-assertions.clj` script until after the enki
`/api/registerkey` calls, and then start bank-ui-A with `ENKI_CALLBACK_URL` set
to the corresponding bank-id UUID (see `enki-bank-web-ui/README.md`).

If alicia's registration to bank-A does actually feed Consus and Enki, then you
can just proceed with alicia's registration to enki-core, and then link the
accounts on enki's web UI.

Otherwise, you need to apply the rest of the above script, except the
last commented line, pretending the registration on bank-A did feed Consus and
Enki. You can then show that the accounts are not linked on enki, by looking at
the dashboard and seeing no data. Then after successfully linking the accounts
on enki-core, you will see data in the dashboard.

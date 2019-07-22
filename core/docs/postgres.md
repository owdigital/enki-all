## PostgreSQL

These instructions are for running PostgreSQL on macOS.

- Download [Postgres.app](https://postgresapp.com/) and install it to `/Applications` folder.
- Open PostgreSQL app. You will notice an elephant in the notification area.
- Click "Initialize" to create a new server.
- Configure your $PATH to use the included command line tools:

```
sudo mkdir -p /etc/paths.d &&
echo /Applications/Postgres.app/Contents/Versions/latest/bin | sudo tee /etc/paths.d/postgresapp
```

### Helpful PostgreSQL commands:

Create a PostgreSQL user:

```
createuser --interactive <user>
```

Create a database:
```
createdb -U <user> <dbname>
```

Delete a database:
```
dropdb -U <user> <dbname>
```
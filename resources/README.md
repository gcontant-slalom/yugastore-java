## Parse metadata

Run the following to parse the metadata JSON file into CSV format that the loader accepts. Remember to replace `metadata_strict_small.json` with the appropriate filename if needed.
```
$ python parse_metadata_json.py metadata_strict_small.json
```

The above should result in following two files that can be loaded into YugabyteDB subsequently:
* `cronos_products.csv`
* `cronos_product_rankings.csv`
* `cronos_product_inventory.csv`


## Loading data

You need [the Cassandra loader for YugabyteDB](https://github.com/YugaByte/cassandra-loader/) in
order to load the data. You can fetch the Cassandra loader by running the following:
```
$ wget https://github.com/YugaByte/cassandra-loader/releases/download/v0.0.27-yb-1/cassandra-loader
$ chmod +x cassandra-loader
```

### Load `cronos_products.csv`

Load the data in `cronos_products.csv` into YugabyteDB by running the following command:
```
$ cassandra-loader -f cronos_products.csv -host localhost -schema \
    "cronos.products(asin, tenant_key, title, description, price, imUrl, also_bought, also_viewed, bought_together, buy_after_viewing, brand, categories, num_reviews, num_stars, avg_stars)"
```

You should see output as follows:
```
*** Processing cronos_products.csv
*** DONE: cronos_products.csv  number of lines processed: 10 (10 inserted)
Lines Processed: 	9  Rate: 	0.0
```

### Load `cronos_product_rankings.csv`

Load the data in `cronos_product_rankings.csv` into YugabyteDB by running the following command:
```
$ cassandra-loader -f cronos_product_rankings.csv -host localhost -schema \
    "cronos.product_rankings(asin, category, tenant_key, sales_rank, title, price, imurl, num_reviews, num_stars, avg_stars)"
```

You should see the following as output:
```
*** Processing cronos_product_rankings.csv
*** DONE: cronos_product_rankings.csv  number of lines processed: 8 (8 inserted)
Lines Processed: 	7  Rate: 	0.0
```

### Load `cronos_product_inventory.csv`

Load the data in `cronos_product_inventory.csv` into YugabyteDB by running the following command:
```
$ cassandra-loader -f cronos_product_inventory.csv -host localhost -schema \
    "cronos.product_inventory(asin, tenant_key, quantity)"
```

The generated seed assets assign all demo rows to the default tenant key `demo-store` so the shared root storefront can keep using the current sample catalog until later ownership-aware flows are implemented.

That default tenant seed supports the shared root route `/` only. New merchant tenants created through `/{tenantSlug}/signup` do not automatically receive copies of these catalog rows, so tenant-route verification and tenant-owned catalog verification are distinct local workflows.

## Querying Data

- To query a product and get its details:
```
cqlsh> SELECT * FROM cronos.products WHERE asin='0000031909';
```

- To query all the categories and the respective sales ranks for a given product:
```
cqlsh> SELECT * FROM cronos.product_rankings WHERE asin='0000031909';

 asin       | category     | sales_rank
------------+--------------+------------
 0000031909 | Toys & Games |     201847

(1 rows)
```

- To query the top 10 products in a given category by sales rank:
```
cqlsh> SELECT * FROM cronos.product_rankings WHERE category='Toys & Games' LIMIT 10;

 category     | sales_rank | asin
--------------+------------+------------
 Toys & Games |     201847 | 0000031909
 Toys & Games |     211836 | 0000031852

(2 rows)
```

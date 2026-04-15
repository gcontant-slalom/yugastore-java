for i in products.json;
do
  python parse_metadata_json.py $i; 
  ./cassandra-loader -f cronos_products.csv -host localhost -schema "cronos.products(asin, tenant_key, title, description, price, imUrl, also_bought, also_viewed, bought_together, buy_after_viewing, brand, categories, num_reviews, num_stars, avg_stars)" -maxInsertErrors 10000 -maxErrors 10000 -charsPerColumn 256000;
  ./cassandra-loader -f cronos_product_rankings.csv -host localhost -schema "cronos.product_rankings(asin, category, tenant_key, sales_rank, title, price, imurl, num_reviews, num_stars, avg_stars)" -maxInsertErrors 10000 -maxErrors 10000 -charsPerColumn 256000;
  ./cassandra-loader -f cronos_product_inventory.csv -host localhost -schema "cronos.product_inventory(asin, tenant_key, quantity)";
  rm *.csv*
done

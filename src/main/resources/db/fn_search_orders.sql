-- ============================================================
-- 주문 검색 함수 (커서 기반 페이징)
-- ============================================================
CREATE OR REPLACE FUNCTION fn_search_orders(
	p_customer_name TEXT DEFAULT NULL,
	p_product_name  TEXT DEFAULT NULL,
	p_status		TEXT DEFAULT NULL,
	p_cursor		BIGINT DEFAULT NULL,
	p_size		  INT DEFAULT 50
)
RETURNS TABLE (
	id			BIGINT,
	customer_name TEXT,
	product_name  TEXT,
	quantity	  INT,
	price		 NUMERIC,
	total_amount  NUMERIC,
	status		TEXT,
	created_at	TIMESTAMP
)
LANGUAGE plpgsql
AS $$
DECLARE
	v_sql TEXT;
BEGIN
	v_sql := 'SELECT id, customer_name, product_name, quantity, price, total_amount, status, created_at FROM orders WHERE 1=1';

	IF p_customer_name IS NOT NULL AND p_customer_name <> '' THEN
		v_sql := v_sql || ' AND customer_name LIKE ''%'' || $1 || ''%''';
	END IF;

	IF p_product_name IS NOT NULL AND p_product_name <> '' THEN
		v_sql := v_sql || ' AND product_name LIKE ''%'' || $2 || ''%''';
	END IF;

	IF p_status IS NOT NULL AND p_status <> '' THEN
		v_sql := v_sql || ' AND status = $3';
	END IF;

	IF p_cursor IS NOT NULL THEN
		v_sql := v_sql || ' AND id > $4';
	END IF;

	v_sql := v_sql || ' ORDER BY id LIMIT $5';

	RETURN QUERY EXECUTE v_sql
		USING p_customer_name, p_product_name, p_status, p_cursor, p_size;
END;
$$;

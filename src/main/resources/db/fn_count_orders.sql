-- ============================================================
-- 주문 카운트 함수
-- ============================================================
CREATE OR REPLACE FUNCTION fn_count_orders(
	p_customer_name TEXT DEFAULT NULL,
	p_product_name  TEXT DEFAULT NULL,
	p_status		TEXT DEFAULT NULL
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
	v_sql   TEXT;
	v_count BIGINT;
BEGIN
	v_sql := 'SELECT COUNT(*) FROM orders WHERE 1=1';

	IF p_customer_name IS NOT NULL AND p_customer_name <> '' THEN
		v_sql := v_sql || ' AND customer_name LIKE ''%'' || $1 || ''%''';
	END IF;

	IF p_product_name IS NOT NULL AND p_product_name <> '' THEN
		v_sql := v_sql || ' AND product_name LIKE ''%'' || $2 || ''%''';
	END IF;

	IF p_status IS NOT NULL AND p_status <> '' THEN
		v_sql := v_sql || ' AND status = $3';
	END IF;

	EXECUTE v_sql INTO v_count
		USING p_customer_name, p_product_name, p_status;

	RETURN v_count;
END;
$$;

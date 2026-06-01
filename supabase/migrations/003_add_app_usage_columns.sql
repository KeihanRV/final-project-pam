ALTER TABLE public.app_usage
ADD COLUMN IF NOT EXISTS package_name text null,
ADD COLUMN IF NOT EXISTS usage_date text null;

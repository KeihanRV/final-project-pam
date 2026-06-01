-- Pastikan RLS aktif pada tabel app_usage
ALTER TABLE public.app_usage ENABLE ROW LEVEL SECURITY;

-- Policy: User hanya bisa SELECT data sendiri
CREATE POLICY IF NOT EXISTS "User can read own app usage"
ON public.app_usage
FOR SELECT
TO authenticated
USING ((select auth.uid()) = user_id);

-- Policy: User hanya bisa INSERT data sendiri
CREATE POLICY IF NOT EXISTS "User can insert own app usage"
ON public.app_usage
FOR INSERT
TO authenticated
WITH CHECK ((select auth.uid()) = user_id);

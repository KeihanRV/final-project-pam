create table public.user_selected_apps (
  id uuid not null default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  package_name text not null,
  app_label text not null,
  unscroll_minutes int not null default 15 check (unscroll_minutes between 1 and 1440),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint user_selected_apps_pkey primary key (id),
  constraint user_selected_apps_unique unique (user_id, package_name)
) tablespace pg_default;

alter table public.user_selected_apps enable row level security;

create policy "User can select own apps"
on public.user_selected_apps
for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "User can insert own apps"
on public.user_selected_apps
for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy "User can update own apps"
on public.user_selected_apps
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "User can delete own apps"
on public.user_selected_apps
for delete
to authenticated
using ((select auth.uid()) = user_id);

create or replace function public.update_updated_at_column()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger set_updated_at
before update on public.user_selected_apps
for each row
execute function public.update_updated_at_column();

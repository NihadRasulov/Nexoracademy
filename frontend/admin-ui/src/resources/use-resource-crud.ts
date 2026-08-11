import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ResourceConfig } from "@/resources/types";

export function useResourceCrud<T extends Record<string, unknown>>(config: ResourceConfig<T>) {
  const queryClient = useQueryClient();
  const queryKey = ["resource", config.key];

  const listQuery = useQuery({
    queryKey,
    queryFn: () => api.get<T[]>(config.apiPath),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey });

  const createMutation = useMutation({
    mutationFn: (body: Partial<T>) => api.post<T>(config.apiPath, body),
    onSuccess: invalidate,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: string | number; body: Partial<T> }) =>
      api.patch<T>(`${config.apiPath}/${id}`, body),
    onSuccess: invalidate,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string | number) => api.delete<void>(`${config.apiPath}/${id}`),
    onSuccess: invalidate,
  });

  return { listQuery, createMutation, updateMutation, deleteMutation };
}

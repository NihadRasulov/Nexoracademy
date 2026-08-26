import { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ChevronDown, Loader2 } from "lucide-react";
import { ImageUploadField } from "@/components/image-upload";
import { MultiReferencePicker, ReferencePicker } from "@/components/reference-picker";
import { KeyValueEditor, StringListInput } from "@/components/structured-inputs";
import type { FieldConfig } from "@/resources/types";
import { toApiValue, toFormValue } from "@/resources/field-transform";

type FormState = Record<string, string | boolean>;

export interface ResourceFormMeta {
  title: string;
  fields: FieldConfig[];
}

interface ResourceFormDialogProps<T extends Record<string, unknown>> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  config: ResourceFormMeta;
  initialValues: T | null;
  submitting: boolean;
  fieldErrors?: Record<string, string> | null;
  onSubmit: (body: Record<string, unknown>) => void;
}

function buildInitialState<T extends Record<string, unknown>>(
  fields: FieldConfig[],
  values: T | null,
): FormState {
  const isCreate = values === null;
  const state: FormState = {};
  for (const field of fields) {
    if (field.autoGenerate === "uuid") {
      // Auto-fill on create, keep existing value on edit — never shown to the admin.
      const existing = values?.[field.name as keyof T];
      state[field.name] = isCreate ? crypto.randomUUID() : existing != null ? String(existing) : crypto.randomUUID();
      continue;
    }
    state[field.name] = toFormValue(field, values?.[field.name as keyof T]);
  }
  return state;
}

export function ResourceFormDialog<T extends Record<string, unknown>>({
  open,
  onOpenChange,
  config,
  initialValues,
  submitting,
  fieldErrors,
  onSubmit,
}: ResourceFormDialogProps<T>) {
  const [state, setState] = useState<FormState>(() => buildInitialState(config.fields, initialValues));
  const [parseError, setParseError] = useState<string | null>(null);
  const [showAdvanced, setShowAdvanced] = useState(false);

  useEffect(() => {
    if (open) {
      setState(buildInitialState(config.fields, initialValues));
      setParseError(null);
      setShowAdvanced(false);
    }
  }, [open, initialValues, config.fields]);

  const isEdit = initialValues !== null;

  const visibleFields = config.fields.filter((f) => f.autoGenerate !== "uuid");
  const primaryFields = visibleFields.filter((f) => !f.advanced);
  const advancedFields = visibleFields.filter((f) => f.advanced);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setParseError(null);
    const body: Record<string, unknown> = {};
    try {
      for (const field of config.fields) {
        const apiValue = toApiValue(field, state[field.name]);
        if (apiValue !== undefined) {
          body[field.name] = apiValue;
        }
      }
    } catch {
      setParseError("Struktur sahələrində xəta var - dəyərləri yoxlayın.");
      return;
    }
    onSubmit(body);
  }

  const renderField = (field: FieldConfig) => (
    <div key={field.name} className="space-y-1.5">
      <Label htmlFor={field.name}>
        {field.label}
        {field.required && <span className="text-destructive"> *</span>}
      </Label>
      <FieldInput
        field={field}
        value={state[field.name]}
        onChange={(value) => setState((s) => ({ ...s, [field.name]: value }))}
      />
      {field.helpText && <p className="text-xs text-muted-foreground">{field.helpText}</p>}
      {fieldErrors?.[field.name] && (
        <p className="text-xs font-medium text-destructive">{fieldErrors[field.name]}</p>
      )}
    </div>
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEdit ? `${config.title} redaktəsi` : `Yeni ${config.title}`}</DialogTitle>
          <DialogDescription>
            {isEdit ? "Dəyişdirmək istədiyiniz sahələri redaktə edin." : "Yeni qeyd yaratmaq üçün məlumatları doldurun."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {primaryFields.map(renderField)}

          {advancedFields.length > 0 && (
            <div className="rounded-lg border border-border">
              <button
                type="button"
                onClick={() => setShowAdvanced((v) => !v)}
                aria-expanded={showAdvanced}
                className="flex w-full items-center justify-between px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
              >
                Ətraflı sahələr
                <ChevronDown className={`size-4 transition-transform ${showAdvanced ? "rotate-180" : ""}`} />
              </button>
              {showAdvanced && <div className="space-y-4 border-t border-border p-3">{advancedFields.map(renderField)}</div>}
            </div>
          )}

          {parseError && <p className="text-sm font-medium text-destructive">{parseError}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Ləğv et
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting && <Loader2 className="size-4 animate-spin" />}
              {isEdit ? "Yadda saxla" : "Yarat"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function FieldInput({
  field,
  value,
  onChange,
}: {
  field: FieldConfig;
  value: string | boolean;
  onChange: (value: string | boolean) => void;
}) {
  switch (field.type) {
    case "boolean":
      return <Switch checked={value as boolean} onCheckedChange={onChange} />;
    case "image":
      return (
        <ImageUploadField
          id={field.name}
          value={(value as string) || ""}
          onChange={onChange}
          placeholder={field.placeholder}
        />
      );
    case "reference":
      return (
        <ReferencePicker
          id={field.name}
          kind={field.refKind!}
          value={value as string}
          onChange={onChange}
          placeholder={field.placeholder}
        />
      );
    case "referenceArray":
      return (
        <MultiReferencePicker
          id={field.name}
          kind={field.refKind!}
          value={value as string}
          onChange={onChange}
          placeholder={field.placeholder}
        />
      );
    case "stringList":
      return <StringListInput id={field.name} value={value as string} onChange={onChange} placeholder={field.placeholder} />;
    case "keyValue":
      return <KeyValueEditor value={value as string} onChange={onChange} />;
    case "textarea":
    case "json":
    case "jsonArray":
      return (
        <Textarea
          id={field.name}
          value={value as string}
          placeholder={field.placeholder}
          onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => onChange(e.target.value)}
          className={field.type !== "textarea" ? "font-mono text-xs" : undefined}
          rows={field.type === "textarea" ? 3 : 5}
        />
      );
    case "select":
      return (
        <Select
          items={field.options}
          value={(value as string) || undefined}
          onValueChange={(v) => onChange(v ?? "")}
        >
          <SelectTrigger id={field.name} className="w-full">
            <SelectValue placeholder={field.placeholder ?? "Seçin"} />
          </SelectTrigger>
          <SelectContent>
            {field.options?.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      );
    case "number":
      return (
        <Input
          id={field.name}
          type="number"
          step="any"
          value={value as string}
          placeholder={field.placeholder}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        />
      );
    case "date":
      return (
        <Input
          id={field.name}
          type="date"
          value={value as string}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        />
      );
    case "datetime":
      return (
        <Input
          id={field.name}
          type="datetime-local"
          value={value as string}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        />
      );
    case "guidArray":
      return (
        <Input
          id={field.name}
          value={value as string}
          placeholder={field.placeholder ?? "uuid-1, uuid-2, ..."}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        />
      );
    default:
      return (
        <Input
          id={field.name}
          value={value as string}
          placeholder={field.placeholder}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        />
      );
  }
}

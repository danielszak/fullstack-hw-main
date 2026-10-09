interface PageStateProps {
  kind: "loading" | "error" | "empty";
  message: string;
}

export function PageState({ kind, message }: PageStateProps) {
  const role = kind === "error" ? "alert" : "status";

  return (
    <div className={`page-state page-state--${kind}`} role={role}>
      {kind === "loading" && <span className="spinner" aria-hidden="true" />}
      <p>{message}</p>
    </div>
  );
}

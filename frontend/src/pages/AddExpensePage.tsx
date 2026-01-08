import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { expenseApi } from '@/lib/api';
import { type ImportResult } from '@/lib/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { FileUp, Loader2, CheckCircle2, XCircle, Upload } from 'lucide-react';

export default function AddExpensePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Single expense form state
  const [formData, setFormData] = useState({
    amount: '',
    category: '',
    merchant: '',
    date: new Date().toISOString().split('T')[0],
    bank: '',
  });

  // CSV import state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<ImportResult | null>(null);

  // Single expense creation mutation
  const createExpenseMutation = useMutation({
    mutationFn: (expense: {
      amount: number;
      category: string;
      merchant: string;
      date: string;
      bank?: string;
    }) => expenseApi.createExpense(expense),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      navigate('/dashboard/expenses');
    },
  });

  // CSV import mutation
  const importExpensesMutation = useMutation({
    mutationFn: (file: File) => expenseApi.importExpenses(file),
    onSuccess: (response) => {
      setImportResult(response.data);
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      setSelectedFile(null);
    },
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createExpenseMutation.mutate({
      amount: parseFloat(formData.amount),
      category: formData.category,
      merchant: formData.merchant,
      date: formData.date,
      bank: formData.bank || undefined,
    });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setImportResult(null);
    }
  };

  const handleImportSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedFile) {
      importExpensesMutation.mutate(selectedFile);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Add Expense</h2>
        <p className="text-muted-foreground">
          Create a single expense or import multiple expenses from a CSV file
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Single Expense Form */}
        <Card>
          <CardHeader>
            <CardTitle>Single Expense</CardTitle>
            <CardDescription>Manually add a single expense entry</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="amount">Amount</Label>
                <Input
                  id="amount"
                  name="amount"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Input
                  id="category"
                  name="category"
                  type="text"
                  placeholder="e.g., Food, Transport, Entertainment"
                  value={formData.category}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="merchant">Merchant</Label>
                <Input
                  id="merchant"
                  name="merchant"
                  type="text"
                  placeholder="e.g., Whole Foods, Uber"
                  value={formData.merchant}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="date">Date</Label>
                <Input
                  id="date"
                  name="date"
                  type="date"
                  value={formData.date}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="bank">Bank (Optional)</Label>
                <Input
                  id="bank"
                  name="bank"
                  type="text"
                  placeholder="e.g., Chase, Bank of America"
                  value={formData.bank}
                  onChange={handleInputChange}
                />
              </div>

              <Button type="submit" className="w-full" disabled={createExpenseMutation.isPending}>
                {createExpenseMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Create Expense
              </Button>

              {createExpenseMutation.isError && (
                <div className="rounded-lg border border-destructive bg-destructive/10 p-3 text-sm text-destructive">
                  {createExpenseMutation.error instanceof Error
                    ? createExpenseMutation.error.message
                    : 'Failed to create expense'}
                </div>
              )}
            </form>
          </CardContent>
        </Card>

        {/* CSV Import */}
        <Card>
          <CardHeader>
            <CardTitle>Import from CSV</CardTitle>
            <CardDescription>
              Upload a CSV file to import multiple expenses at once
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-lg bg-muted p-4 text-sm">
              <p className="mb-2 font-medium">CSV Format:</p>
              <code className="block whitespace-pre text-xs">
                date,merchant,amount,bank,category
              </code>
              <p className="mt-2 text-muted-foreground">
                Date format: YYYY-MM-DD. Maximum file size: 1MB. Maximum rows: 1000.
              </p>
            </div>

            <form onSubmit={handleImportSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="csv-file">Select CSV File</Label>
                <div className="flex items-center gap-2">
                  <Input
                    id="csv-file"
                    type="file"
                    accept=".csv,text/csv,application/csv,application/vnd.ms-excel"
                    onChange={handleFileChange}
                    className="flex-1"
                  />
                  {selectedFile && (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        setSelectedFile(null);
                        setImportResult(null);
                        const fileInput = document.getElementById('csv-file') as HTMLInputElement;
                        if (fileInput) fileInput.value = '';
                      }}
                    >
                      Clear
                    </Button>
                  )}
                </div>
                {selectedFile && (
                  <p className="text-sm text-muted-foreground">
                    Selected: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(2)} KB)
                  </p>
                )}
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={!selectedFile || importExpensesMutation.isPending}
              >
                {importExpensesMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Importing...
                  </>
                ) : (
                  <>
                    <Upload className="mr-2 h-4 w-4" />
                    Import Expenses
                  </>
                )}
              </Button>
            </form>

            {importExpensesMutation.isError && (
              <div className="rounded-lg border border-destructive bg-destructive/10 p-3 text-sm text-destructive">
                {importExpensesMutation.error instanceof Error
                  ? importExpensesMutation.error.message
                  : 'Failed to import expenses'}
              </div>
            )}

            {importResult && (
              <div className="space-y-3 rounded-lg border p-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold">Import Results</h3>
                  {importResult.failedImports === 0 ? (
                    <CheckCircle2 className="h-5 w-5 text-green-600" />
                  ) : (
                    <XCircle className="h-5 w-5 text-destructive" />
                  )}
                </div>

                <Separator />

                <div className="grid grid-cols-3 gap-4 text-center">
                  <div>
                    <p className="text-2xl font-bold">{importResult.totalRows}</p>
                    <p className="text-xs text-muted-foreground">Total Rows</p>
                  </div>
                  <div>
                    <p className="text-2xl font-bold text-green-600">
                      {importResult.successfulImports}
                    </p>
                    <p className="text-xs text-muted-foreground">Successful</p>
                  </div>
                  <div>
                    <p className="text-2xl font-bold text-destructive">
                      {importResult.failedImports}
                    </p>
                    <p className="text-xs text-muted-foreground">Failed</p>
                  </div>
                </div>

                {importResult.errors.length > 0 && (
                  <>
                    <Separator />
                    <div>
                      <p className="mb-2 text-sm font-medium text-destructive">Errors:</p>
                      <div className="max-h-32 space-y-1 overflow-y-auto text-xs">
                        {importResult.errors.map((error, index) => (
                          <p key={index} className="text-muted-foreground">
                            {error}
                          </p>
                        ))}
                      </div>
                    </div>
                  </>
                )}

                {importResult.successfulImports > 0 && (
                  <Button
                    onClick={() => navigate('/dashboard/expenses')}
                    variant="outline"
                    className="w-full"
                  >
                    View Expenses
                  </Button>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

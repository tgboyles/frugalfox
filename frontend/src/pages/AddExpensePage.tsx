import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { expenseApi } from '@/lib/api';
import { type ImportResult } from '@/lib/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { AlertCircle, CheckCircle2, FileUp, HelpCircle, Plus, Upload, XCircle } from 'lucide-react';

type ExpenseFormData = {
  amount: string;
  category: string;
  merchant: string;
  date: string;
  bank: string;
};

// Constants
const REDIRECT_DELAY_MS = 1000;
const BYTES_PER_KB = 1024;

export default function AddExpensePage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Individual expense form state
  const [formData, setFormData] = useState<ExpenseFormData>({
    amount: '',
    category: '',
    merchant: '',
    date: new Date().toISOString().split('T')[0], // Default to today
    bank: '',
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  // CSV upload state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<ImportResult | null>(null);

  // Create expense mutation
  const createMutation = useMutation({
    mutationFn: async (data: ExpenseFormData) => {
      return await expenseApi.createExpense({
        amount: parseFloat(data.amount),
        category: data.category,
        merchant: data.merchant,
        date: data.date,
        bank: data.bank || undefined,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      // Reset form
      setFormData({
        amount: '',
        category: '',
        merchant: '',
        date: new Date().toISOString().split('T')[0],
        bank: '',
      });
      setFormErrors({});
      // Navigate to expenses page after a brief delay
      setTimeout(() => navigate('/dashboard/expenses'), REDIRECT_DELAY_MS);
    },
    onError: (error: unknown) => {
      // Handle validation errors from backend
      const err = error as { response?: { data?: { message?: string } } };
      if (err.response?.data?.message) {
        setFormErrors({ submit: err.response.data.message });
      }
    },
  });

  // Import expenses mutation
  const importMutation = useMutation({
    mutationFn: async (file: File) => {
      return await expenseApi.importExpenses(file);
    },
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      setImportResult(response.data);
      setSelectedFile(null);
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string } } };
      const errorMessage =
        err.response?.data?.message || 'Failed to import expenses. Please try again.';
      setImportResult({
        totalRows: 0,
        successfulImports: 0,
        failedImports: 0,
        errors: [errorMessage],
      });
    },
  });

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      errors.amount = 'Amount must be greater than 0';
    }
    if (!formData.category.trim()) {
      errors.category = 'Category is required';
    }
    if (!formData.merchant.trim()) {
      errors.merchant = 'Merchant is required';
    }
    if (!formData.date) {
      errors.date = 'Date is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      createMutation.mutate(formData);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setImportResult(null); // Clear previous results
    }
  };

  const handleFileUpload = () => {
    if (selectedFile) {
      importMutation.mutate(selectedFile);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Add Expense</h2>
        <p className="text-muted-foreground">
          Create a new expense entry or import multiple expenses
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Individual Expense Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Plus className="h-5 w-5" />
              Create Single Expense
            </CardTitle>
            <CardDescription>Add a new expense entry manually</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="date">Date *</Label>
                <Input
                  id="date"
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                  aria-invalid={!!formErrors.date}
                />
                {formErrors.date && <p className="text-destructive text-sm">{formErrors.date}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="merchant">Merchant *</Label>
                <Input
                  id="merchant"
                  placeholder="e.g., Starbucks, Amazon, Target"
                  value={formData.merchant}
                  onChange={(e) => setFormData({ ...formData, merchant: e.target.value })}
                  aria-invalid={!!formErrors.merchant}
                />
                {formErrors.merchant && (
                  <p className="text-destructive text-sm">{formErrors.merchant}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="amount">Amount *</Label>
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  aria-invalid={!!formErrors.amount}
                />
                {formErrors.amount && (
                  <p className="text-destructive text-sm">{formErrors.amount}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="category">Category *</Label>
                <Input
                  id="category"
                  placeholder="e.g., Food, Transport, Entertainment"
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  aria-invalid={!!formErrors.category}
                />
                {formErrors.category && (
                  <p className="text-destructive text-sm">{formErrors.category}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="bank">Bank (Optional)</Label>
                <Input
                  id="bank"
                  placeholder="e.g., Chase, Bank of America"
                  value={formData.bank}
                  onChange={(e) => setFormData({ ...formData, bank: e.target.value })}
                />
              </div>

              {formErrors.submit && (
                <div className="text-destructive flex items-center gap-2 text-sm">
                  <XCircle className="h-4 w-4" />
                  {formErrors.submit}
                </div>
              )}

              {createMutation.isSuccess && (
                <div className="flex items-center gap-2 text-sm text-green-600">
                  <CheckCircle2 className="h-4 w-4" />
                  Expense created successfully! Redirecting...
                </div>
              )}

              <Button type="submit" className="w-full" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Creating...' : 'Create Expense'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* CSV Bulk Upload Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileUp className="h-5 w-5" />
              Bulk Import from CSV
              <Tooltip>
                <TooltipTrigger asChild>
                  <button type="button" className="text-muted-foreground hover:text-foreground">
                    <HelpCircle className="h-4 w-4" />
                  </button>
                </TooltipTrigger>
                <TooltipContent className="max-w-sm">
                  <div className="space-y-2">
                    <p className="font-semibold">CSV Format Requirements:</p>
                    <p className="text-xs">Your CSV file must have these columns in order:</p>
                    <code className="bg-muted block rounded p-2 text-xs">
                      date,merchant,amount,bank,category
                    </code>
                    <p className="text-xs">
                      <strong>Example:</strong>
                    </p>
                    <code className="bg-muted block rounded p-2 font-mono text-xs">
                      2024-01-15,Starbucks,5.50,Chase,Food{'\n'}
                      2024-01-16,Amazon,29.99,Chase,Shopping
                    </code>
                    <p className="text-xs">
                      • Date format: YYYY-MM-DD{'\n'}• Amount: positive decimal number{'\n'}• Max
                      file size: 1MB{'\n'}• Max rows: 1000
                    </p>
                  </div>
                </TooltipContent>
              </Tooltip>
            </CardTitle>
            <CardDescription>Upload a CSV file with multiple expenses</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="csv-file">Select CSV File</Label>
              <div className="flex gap-2">
                <Input
                  id="csv-file"
                  type="file"
                  accept=".csv,text/csv"
                  onChange={handleFileChange}
                  className="flex-1"
                />
              </div>
              {selectedFile && (
                <p className="text-muted-foreground text-sm">
                  Selected: {selectedFile.name} ({(selectedFile.size / BYTES_PER_KB).toFixed(2)} KB)
                </p>
              )}
            </div>

            <Button
              onClick={handleFileUpload}
              disabled={!selectedFile || importMutation.isPending}
              className="w-full"
            >
              <Upload className="mr-2 h-4 w-4" />
              {importMutation.isPending ? 'Uploading...' : 'Upload and Import'}
            </Button>

            {/* Import Results */}
            {importResult && (
              <div className="space-y-3 rounded-lg border p-4">
                <h4 className="font-semibold">Import Results</h4>

                <div className="space-y-2 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Total Rows:</span>
                    <span className="font-medium">{importResult.totalRows}</span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-1 text-green-600">
                      <CheckCircle2 className="h-4 w-4" />
                      Successful:
                    </span>
                    <span className="font-medium text-green-600">
                      {importResult.successfulImports}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-destructive flex items-center gap-1">
                      <XCircle className="h-4 w-4" />
                      Failed:
                    </span>
                    <span className="text-destructive font-medium">
                      {importResult.failedImports}
                    </span>
                  </div>
                </div>

                {importResult.errors.length > 0 && (
                  <div className="space-y-2">
                    <div className="text-destructive flex items-center gap-2 text-sm font-medium">
                      <AlertCircle className="h-4 w-4" />
                      Errors:
                    </div>
                    <div className="bg-destructive/10 max-h-40 space-y-1 overflow-y-auto rounded-md p-2">
                      {importResult.errors.map((error, index) => (
                        <p key={index} className="text-destructive text-xs">
                          {error}
                        </p>
                      ))}
                    </div>
                  </div>
                )}

                {importResult.successfulImports > 0 && (
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full"
                    onClick={() => navigate('/dashboard/expenses')}
                  >
                    View Imported Expenses
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

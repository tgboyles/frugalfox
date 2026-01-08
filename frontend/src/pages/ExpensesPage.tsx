import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { expenseApi } from '@/lib/api';
import { type Expense, type PageableResponse } from '@/lib/types';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { ChevronLeft, ChevronRight, Pencil, Search, X } from 'lucide-react';
import { Card } from '@/components/ui/card';

type SearchFilters = {
  merchant?: string;
  category?: string;
  bank?: string;
  minAmount?: string;
  maxAmount?: string;
  startDate?: string;
  endDate?: string;
};

export default function ExpensesPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [searchFilters, setSearchFilters] = useState<SearchFilters>({});
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingExpense, setEditingExpense] = useState<Expense | null>(null);
  const [editForm, setEditForm] = useState({
    amount: '',
    category: '',
    merchant: '',
    date: '',
    bank: '',
  });

  // Fetch expenses with pagination and filters
  const { data, isLoading, error } = useQuery({
    queryKey: ['expenses', page, pageSize, searchFilters],
    queryFn: async () => {
      const params: Record<string, string | number> = {
        page,
        size: pageSize,
        sort: 'date,desc',
      };

      // Add search filters
      if (searchFilters.merchant) params.merchant = searchFilters.merchant;
      if (searchFilters.category) params.category = searchFilters.category;
      if (searchFilters.bank) params.bank = searchFilters.bank;
      if (searchFilters.minAmount) params.minAmount = parseFloat(searchFilters.minAmount);
      if (searchFilters.maxAmount) params.maxAmount = parseFloat(searchFilters.maxAmount);
      if (searchFilters.startDate) params.startDate = searchFilters.startDate;
      if (searchFilters.endDate) params.endDate = searchFilters.endDate;

      const response = await expenseApi.getExpenses(params);
      return response.data as PageableResponse<Expense>;
    },
  });

  // Update expense mutation
  const updateMutation = useMutation({
    mutationFn: async (expense: { id: number; data: typeof editForm }) => {
      return await expenseApi.updateExpense(expense.id, {
        amount: parseFloat(expense.data.amount),
        category: expense.data.category,
        merchant: expense.data.merchant,
        date: expense.data.date,
        bank: expense.data.bank,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      setIsEditDialogOpen(false);
      setEditingExpense(null);
    },
  });

  const handleEdit = (expense: Expense) => {
    setEditingExpense(expense);
    setEditForm({
      amount: expense.amount.toString(),
      category: expense.category,
      merchant: expense.merchant,
      date: expense.date,
      bank: expense.bank || '',
    });
    setIsEditDialogOpen(true);
  };

  const handleSaveEdit = () => {
    if (editingExpense) {
      updateMutation.mutate({ id: editingExpense.id, data: editForm });
    }
  };

  const handleSearchChange = (field: keyof SearchFilters, value: string) => {
    setSearchFilters((prev) => ({
      ...prev,
      [field]: value || undefined,
    }));
    setPage(0); // Reset to first page on search
  };

  const clearFilters = () => {
    setSearchFilters({});
    setPage(0);
  };

  const hasActiveFilters = Object.values(searchFilters).some((v) => v !== undefined && v !== '');

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Expenses</h2>
        <p className="text-muted-foreground">Manage and view all your expenses</p>
      </div>

      {/* Search Filters */}
      <Card className="p-4">
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="flex items-center gap-2 text-lg font-semibold">
              <Search className="h-5 w-5" />
              Search & Filter
            </h3>
            {hasActiveFilters && (
              <Button variant="outline" size="sm" onClick={clearFilters}>
                <X className="mr-2 h-4 w-4" />
                Clear Filters
              </Button>
            )}
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="merchant-search">Merchant</Label>
              <Input
                id="merchant-search"
                placeholder="Search by merchant..."
                value={searchFilters.merchant || ''}
                onChange={(e) => handleSearchChange('merchant', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="category-search">Category</Label>
              <Input
                id="category-search"
                placeholder="Search by category..."
                value={searchFilters.category || ''}
                onChange={(e) => handleSearchChange('category', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="bank-search">Bank</Label>
              <Input
                id="bank-search"
                placeholder="Search by bank..."
                value={searchFilters.bank || ''}
                onChange={(e) => handleSearchChange('bank', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="min-amount">Min Amount</Label>
              <Input
                id="min-amount"
                type="number"
                step="0.01"
                placeholder="0.00"
                value={searchFilters.minAmount || ''}
                onChange={(e) => handleSearchChange('minAmount', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="max-amount">Max Amount</Label>
              <Input
                id="max-amount"
                type="number"
                step="0.01"
                placeholder="0.00"
                value={searchFilters.maxAmount || ''}
                onChange={(e) => handleSearchChange('maxAmount', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="start-date">Start Date</Label>
              <Input
                id="start-date"
                type="date"
                value={searchFilters.startDate || ''}
                onChange={(e) => handleSearchChange('startDate', e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="end-date">End Date</Label>
              <Input
                id="end-date"
                type="date"
                value={searchFilters.endDate || ''}
                onChange={(e) => handleSearchChange('endDate', e.target.value)}
              />
            </div>
          </div>
        </div>
      </Card>

      {/* Results and Pagination Controls */}
      <div className="flex items-center justify-between">
        <div className="text-muted-foreground text-sm">
          {data && (
            <>
              Showing {data.numberOfElements} of {data.totalElements} expense
              {data.totalElements !== 1 ? 's' : ''}
            </>
          )}
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Label htmlFor="page-size" className="text-sm">
              Per page:
            </Label>
            <Select
              value={pageSize.toString()}
              onValueChange={(value) => {
                setPageSize(parseInt(value));
                setPage(0);
              }}
            >
              <SelectTrigger id="page-size" className="w-20">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="10">10</SelectItem>
                <SelectItem value="20">20</SelectItem>
                <SelectItem value="50">50</SelectItem>
                <SelectItem value="100">100</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={!data || data.first}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm">
              Page {data ? data.number + 1 : 1} of {data?.totalPages || 1}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => p + 1)}
              disabled={!data || data.last}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Expenses Table */}
      <Card>
        <div className="overflow-x-auto">
          {isLoading ? (
            <div className="text-muted-foreground p-8 text-center">Loading expenses...</div>
          ) : error ? (
            <div className="text-destructive p-8 text-center">
              Error loading expenses. Please try again.
            </div>
          ) : !data || data.content.length === 0 ? (
            <div className="text-muted-foreground p-8 text-center">
              {hasActiveFilters
                ? 'No expenses found matching your search criteria.'
                : 'No expenses found. Start by adding your first expense!'}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Merchant</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Bank</TableHead>
                  <TableHead className="text-right">Amount</TableHead>
                  <TableHead className="w-[80px]">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.content.map((expense) => (
                  <TableRow key={expense.id}>
                    <TableCell className="font-medium">
                      {new Date(expense.date).toLocaleDateString()}
                    </TableCell>
                    <TableCell>{expense.merchant}</TableCell>
                    <TableCell>{expense.category}</TableCell>
                    <TableCell>{expense.bank}</TableCell>
                    <TableCell className="text-right font-medium">
                      ${expense.amount.toFixed(2)}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEdit(expense)}
                        className="h-8 w-8 p-0"
                      >
                        <Pencil className="h-4 w-4" />
                        <span className="sr-only">Edit expense</span>
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </div>
      </Card>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edit Expense</DialogTitle>
            <DialogDescription>
              Make changes to your expense. Click save when you're done.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="edit-merchant">Merchant</Label>
              <Input
                id="edit-merchant"
                value={editForm.merchant}
                onChange={(e) => setEditForm({ ...editForm, merchant: e.target.value })}
                placeholder="Enter merchant name"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-category">Category</Label>
              <Input
                id="edit-category"
                value={editForm.category}
                onChange={(e) => setEditForm({ ...editForm, category: e.target.value })}
                placeholder="Enter category"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-bank">Bank</Label>
              <Input
                id="edit-bank"
                value={editForm.bank}
                onChange={(e) => setEditForm({ ...editForm, bank: e.target.value })}
                placeholder="Enter bank name"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-amount">Amount</Label>
              <Input
                id="edit-amount"
                type="number"
                step="0.01"
                value={editForm.amount}
                onChange={(e) => setEditForm({ ...editForm, amount: e.target.value })}
                placeholder="0.00"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-date">Date</Label>
              <Input
                id="edit-date"
                type="date"
                value={editForm.date}
                onChange={(e) => setEditForm({ ...editForm, date: e.target.value })}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveEdit} disabled={updateMutation.isPending}>
              {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useQuery } from '@tanstack/react-query';
import { expenseApi } from '@/lib/api';
import { type Expense } from '@/lib/types';
import { TrendingUp, TrendingDown, Receipt, DollarSign } from 'lucide-react';

export default function DashboardHome() {
  const { data: expensesData, isLoading } = useQuery({
    queryKey: ['expenses', { page: 0, size: 100 }],
    queryFn: () => expenseApi.getExpenses({ page: 0, size: 100 }),
  });

  const expenses = expensesData?.data.content || [];

  // Calculate metrics
  const totalExpenses = expenses.reduce((sum: number, expense: Expense) => sum + expense.amount, 0);
  const averageExpense = expenses.length > 0 ? totalExpenses / expenses.length : 0;
  const categoryCount = new Set(expenses.map((e: Expense) => e.category)).size;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">Overview of your expense tracking</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-muted-foreground text-sm font-medium">Total Expenses</p>
              <p className="text-2xl font-bold">${isLoading ? '...' : totalExpenses.toFixed(2)}</p>
            </div>
            <DollarSign className="text-muted-foreground h-8 w-8" />
          </div>
          <div className="mt-4 flex items-center text-sm">
            <Badge variant="outline" className="gap-1">
              <TrendingUp className="h-3 w-3" />
              {expenses.length} transactions
            </Badge>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-muted-foreground text-sm font-medium">Average Expense</p>
              <p className="text-2xl font-bold">${isLoading ? '...' : averageExpense.toFixed(2)}</p>
            </div>
            <Receipt className="text-muted-foreground h-8 w-8" />
          </div>
          <div className="mt-4 flex items-center text-sm">
            <Badge variant="outline" className="gap-1">
              Per transaction
            </Badge>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-muted-foreground text-sm font-medium">Categories</p>
              <p className="text-2xl font-bold">{isLoading ? '...' : categoryCount}</p>
            </div>
            <TrendingUp className="text-muted-foreground h-8 w-8" />
          </div>
          <div className="mt-4 flex items-center text-sm">
            <Badge variant="outline" className="gap-1">
              Active categories
            </Badge>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-muted-foreground text-sm font-medium">This Month</p>
              <p className="text-2xl font-bold">${isLoading ? '...' : '0.00'}</p>
            </div>
            <TrendingDown className="text-muted-foreground h-8 w-8" />
          </div>
          <div className="mt-4 flex items-center text-sm">
            <Badge variant="outline" className="gap-1">
              Current month
            </Badge>
          </div>
        </Card>
      </div>

      <Card className="p-6">
        <h3 className="mb-4 text-lg font-semibold">Recent Expenses</h3>
        {isLoading ? (
          <p className="text-muted-foreground">Loading...</p>
        ) : expenses.length === 0 ? (
          <p className="text-muted-foreground">
            No expenses yet. Add your first expense to get started!
          </p>
        ) : (
          <div className="space-y-4">
            {expenses.slice(0, 5).map((expense: Expense) => (
              <div
                key={expense.id}
                className="flex items-center justify-between border-b pb-4 last:border-0 last:pb-0"
              >
                <div>
                  <p className="font-medium">{expense.merchant}</p>
                  <p className="text-muted-foreground text-sm">
                    {expense.category} • {expense.expenseDate}
                  </p>
                </div>
                <p className="font-semibold">${expense.amount.toFixed(2)}</p>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useQuery } from '@tanstack/react-query';
import { expenseApi } from '@/lib/api';
import { type Expense } from '@/lib/types';
import { TrendingUp, TrendingDown, Receipt, DollarSign } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { useMemo } from 'react';

// Color palette for charts
const COLORS = [
  '#f97316', // orange (accent color)
  '#0ea5e9', // sky blue
  '#8b5cf6', // purple
  '#10b981', // green
  '#f59e0b', // amber
  '#ec4899', // pink
  '#06b6d4', // cyan
  '#6366f1', // indigo
  '#84cc16', // lime
  '#ef4444', // red
];

export default function DashboardHome() {
  const { data: expensesData, isLoading } = useQuery({
    queryKey: ['expenses', { page: 0, size: 1000 }],
    queryFn: () => expenseApi.getExpenses({ page: 0, size: 1000 }),
    // Note: Fetching 1000 records for comprehensive analytics.
    // For production with large datasets, consider implementing server-side aggregation
  });

  const expenses = expensesData?.data.content || [];

  // Calculate metrics - memoized to avoid recalculation on every render
  const { totalExpenses, averageExpense, categoryCount } = useMemo(() => {
    const total = expenses.reduce((sum: number, expense: Expense) => sum + expense.amount, 0);
    const average = expenses.length > 0 ? total / expenses.length : 0;
    const categories = new Set(expenses.map((e: Expense) => e.category)).size;
    return {
      totalExpenses: total,
      averageExpense: average,
      categoryCount: categories,
    };
  }, [expenses]);

  // Aggregate data by category - memoized to avoid recalculation on every render
  const categoryChartData = useMemo(() => {
    const categoryData = expenses.reduce((acc: Record<string, number>, expense: Expense) => {
      acc[expense.category] = (acc[expense.category] || 0) + expense.amount;
      return acc;
    }, {});

    return Object.entries(categoryData).map(([name, value]) => ({
      name,
      value: Number((value as number).toFixed(2)),
    }));
  }, [expenses]);

  // Aggregate data by bank - memoized to avoid recalculation on every render
  const bankChartData = useMemo(() => {
    const bankData = expenses.reduce((acc: Record<string, number>, expense: Expense) => {
      const bank = expense.bank || 'Unknown';
      acc[bank] = (acc[bank] || 0) + expense.amount;
      return acc;
    }, {});

    return Object.entries(bankData).map(([name, value]) => ({
      name,
      value: Number((value as number).toFixed(2)),
    }));
  }, [expenses]);

  // Separate income and expenses - memoized to avoid recalculation on every render
  // Business logic: Negative amounts represent income (e.g., returns, reimbursements)
  // Positive amounts represent regular expenses
  const incomeExpenseChartData = useMemo(() => {
    const incomeExpenseData = expenses.reduce(
      (acc: { income: number; expenses: number }, expense: Expense) => {
        if (expense.amount < 0) {
          acc.income += Math.abs(expense.amount);
        } else {
          acc.expenses += expense.amount;
        }
        return acc;
      },
      { income: 0, expenses: 0 }
    );

    return [
      { name: 'Income', value: Number(incomeExpenseData.income.toFixed(2)), color: '#10b981' },
      { name: 'Expenses', value: Number(incomeExpenseData.expenses.toFixed(2)), color: '#f97316' },
    ].filter((item) => item.value > 0); // Only show non-zero values
  }, [expenses]);

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

      {/* Pie Charts Section */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Spend by Category */}
        <Card className="p-6">
          <h3 className="mb-4 text-lg font-semibold">Spend by Category</h3>
          {isLoading ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center">
              Loading...
            </div>
          ) : categoryChartData.length === 0 ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center text-center">
              No expense data available
            </div>
          ) : (
            <ResponsiveContainer
              width="100%"
              height={300}
              role="img"
              aria-label="Pie chart showing spending distribution by category"
            >
              <PieChart>
                <Pie
                  data={categoryChartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ percent }) => `${((percent ?? 0) * 100).toFixed(0)}%`}
                  outerRadius={80}
                  dataKey="value"
                >
                  {categoryChartData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Card>

        {/* Spend by Bank */}
        <Card className="p-6">
          <h3 className="mb-4 text-lg font-semibold">Spend by Bank</h3>
          {isLoading ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center">
              Loading...
            </div>
          ) : bankChartData.length === 0 ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center text-center">
              No bank data available
            </div>
          ) : (
            <ResponsiveContainer
              width="100%"
              height={300}
              role="img"
              aria-label="Pie chart showing spending distribution by bank"
            >
              <PieChart>
                <Pie
                  data={bankChartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ percent }) => `${((percent ?? 0) * 100).toFixed(0)}%`}
                  outerRadius={80}
                  dataKey="value"
                >
                  {bankChartData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Card>

        {/* Income vs Expenses */}
        <Card className="p-6">
          <h3 className="mb-4 text-lg font-semibold">Income vs Expenses</h3>
          {isLoading ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center">
              Loading...
            </div>
          ) : incomeExpenseChartData.length === 0 ? (
            <div className="text-muted-foreground flex h-[300px] items-center justify-center text-center">
              No data available
            </div>
          ) : (
            <ResponsiveContainer
              width="100%"
              height={300}
              role="img"
              aria-label="Pie chart showing income versus expenses"
            >
              <PieChart>
                <Pie
                  data={incomeExpenseChartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ percent }) => `${((percent ?? 0) * 100).toFixed(0)}%`}
                  outerRadius={80}
                  dataKey="value"
                >
                  {incomeExpenseChartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Card>
      </div>
    </div>
  );
}

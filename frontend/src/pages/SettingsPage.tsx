import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { settingsApi, expenseApi } from '@/lib/api';
import { useAuth } from '@/contexts/AuthContext';
import { type MessageResponse, type UserResponse } from '@/lib/types';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Download, Lock, Mail, FileDown } from 'lucide-react';

export default function SettingsPage() {
  const { username } = useAuth();
  const [emailForm, setEmailForm] = useState({ email: '' });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [exportFilters, setExportFilters] = useState({
    category: '',
    merchant: '',
    bank: '',
    minAmount: '',
    maxAmount: '',
    startDate: '',
    endDate: '',
  });

  // Fetch current user information
  const { data: currentUser, error: currentUserError, isLoading: isLoadingUser } = useQuery({
    queryKey: ['currentUser'],
    queryFn: async () => {
      const response = await settingsApi.getCurrentUser();
      return response.data;
    },
  });

  // Email update mutation
  const emailMutation = useMutation({
    mutationFn: async (email: string) => {
      const response = await settingsApi.updateEmail(email);
      return response.data as UserResponse;
    },
    onSuccess: () => {
      setEmailForm({ email: '' });
      alert('Email updated successfully!');
    },
    onError: (error: { response?: { data?: { message?: string } } }) => {
      alert(error.response?.data?.message || 'Failed to update email');
    },
  });

  // Password update mutation
  const passwordMutation = useMutation({
    mutationFn: async (data: { currentPassword: string; newPassword: string }) => {
      const response = await settingsApi.updatePassword(data.currentPassword, data.newPassword);
      return response.data as MessageResponse;
    },
    onSuccess: () => {
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      alert('Password updated successfully!');
    },
    onError: (error: { response?: { data?: { message?: string } } }) => {
      alert(error.response?.data?.message || 'Failed to update password');
    },
  });

  // CSV export mutation
  const exportMutation = useMutation({
    mutationFn: async () => {
      const params: Record<string, string | number> = {};

      if (exportFilters.category) params.category = exportFilters.category;
      if (exportFilters.merchant) params.merchant = exportFilters.merchant;
      if (exportFilters.bank) params.bank = exportFilters.bank;
      if (exportFilters.minAmount) params.minAmount = parseFloat(exportFilters.minAmount);
      if (exportFilters.maxAmount) params.maxAmount = parseFloat(exportFilters.maxAmount);
      if (exportFilters.startDate) params.startDate = exportFilters.startDate;
      if (exportFilters.endDate) params.endDate = exportFilters.endDate;

      const response = await expenseApi.exportExpenses(params);
      return response.data as Blob;
    },
    onSuccess: (blob) => {
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `expenses-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      alert('Expenses exported successfully!');
    },
    onError: (error: { response?: { data?: { message?: string } } }) => {
      alert(error.response?.data?.message || 'Failed to export expenses');
    },
  });

  const handleEmailSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailForm.email) {
      alert('Please enter an email address');
      return;
    }
    emailMutation.mutate(emailForm.email);
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      alert('Please fill in all password fields');
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      alert('New passwords do not match');
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      alert('New password must be at least 8 characters');
      return;
    }
    passwordMutation.mutate({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    });
  };

  const handleExport = () => {
    exportMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Settings</h2>
        <p className="text-muted-foreground">Manage your account settings and preferences</p>
      </div>

      {/* Account Information */}
      <Card className="p-6">
        <h3 className="mb-4 text-xl font-semibold">Account Information</h3>
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-muted-foreground">Username:</span>
            <span className="font-medium">{username}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-muted-foreground">Email:</span>
            <span className="font-medium">
              {currentUserError
                ? 'Error loading email'
                : isLoadingUser
                  ? 'Loading...'
                  : currentUser?.email || 'Not available'}
            </span>
          </div>
        </div>
      </Card>

      <Separator />

      {/* Email Update */}
      <Card className="p-6">
        <div className="mb-4 flex items-center gap-2">
          <Mail className="h-5 w-5" />
          <h3 className="text-xl font-semibold">Update Email</h3>
        </div>
        <form onSubmit={handleEmailSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="new-email">New Email Address</Label>
            <Input
              id="new-email"
              type="email"
              placeholder="Enter new email address"
              value={emailForm.email}
              onChange={(e) => setEmailForm({ email: e.target.value })}
            />
          </div>
          <Button type="submit" disabled={emailMutation.isPending}>
            {emailMutation.isPending ? 'Updating...' : 'Update Email'}
          </Button>
        </form>
      </Card>

      <Separator />

      {/* Password Update */}
      <Card className="p-6">
        <div className="mb-4 flex items-center gap-2">
          <Lock className="h-5 w-5" />
          <h3 className="text-xl font-semibold">Change Password</h3>
        </div>
        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="current-password">Current Password</Label>
            <Input
              id="current-password"
              type="password"
              placeholder="Enter current password"
              value={passwordForm.currentPassword}
              onChange={(e) =>
                setPasswordForm({ ...passwordForm, currentPassword: e.target.value })
              }
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="new-password">New Password</Label>
            <Input
              id="new-password"
              type="password"
              placeholder="Enter new password (min 8 characters)"
              value={passwordForm.newPassword}
              onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirm-password">Confirm New Password</Label>
            <Input
              id="confirm-password"
              type="password"
              placeholder="Re-enter new password"
              value={passwordForm.confirmPassword}
              onChange={(e) =>
                setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
              }
            />
          </div>
          <Button type="submit" disabled={passwordMutation.isPending}>
            {passwordMutation.isPending ? 'Updating...' : 'Change Password'}
          </Button>
        </form>
      </Card>

      <Separator />

      {/* Export Data */}
      <Card className="p-6">
        <div className="mb-4 flex items-center gap-2">
          <FileDown className="h-5 w-5" />
          <h3 className="text-xl font-semibold">Export Expenses</h3>
        </div>
        <p className="text-muted-foreground mb-4 text-sm">
          Download your expense data as a CSV file. You can apply filters to export specific
          expenses.
        </p>

        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="export-category">Category</Label>
              <Input
                id="export-category"
                placeholder="Filter by category"
                value={exportFilters.category}
                onChange={(e) => setExportFilters({ ...exportFilters, category: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-merchant">Merchant</Label>
              <Input
                id="export-merchant"
                placeholder="Filter by merchant"
                value={exportFilters.merchant}
                onChange={(e) => setExportFilters({ ...exportFilters, merchant: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-bank">Bank</Label>
              <Input
                id="export-bank"
                placeholder="Filter by bank"
                value={exportFilters.bank}
                onChange={(e) => setExportFilters({ ...exportFilters, bank: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-min-amount">Min Amount</Label>
              <Input
                id="export-min-amount"
                type="number"
                step="0.01"
                placeholder="0.00"
                value={exportFilters.minAmount}
                onChange={(e) => setExportFilters({ ...exportFilters, minAmount: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-max-amount">Max Amount</Label>
              <Input
                id="export-max-amount"
                type="number"
                step="0.01"
                placeholder="0.00"
                value={exportFilters.maxAmount}
                onChange={(e) => setExportFilters({ ...exportFilters, maxAmount: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-start-date">Start Date</Label>
              <Input
                id="export-start-date"
                type="date"
                value={exportFilters.startDate}
                onChange={(e) => setExportFilters({ ...exportFilters, startDate: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="export-end-date">End Date</Label>
              <Input
                id="export-end-date"
                type="date"
                value={exportFilters.endDate}
                onChange={(e) => setExportFilters({ ...exportFilters, endDate: e.target.value })}
              />
            </div>
          </div>

          <Button onClick={handleExport} disabled={exportMutation.isPending}>
            <Download className="mr-2 h-4 w-4" />
            {exportMutation.isPending ? 'Exporting...' : 'Download CSV'}
          </Button>
        </div>
      </Card>
    </div>
  );
}

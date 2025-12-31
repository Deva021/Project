import { render, screen } from '@testing-library/react';
import Home from '@/app/page';

describe('Home Page', () => {
  it('renders the welcome message', () => {
    render(<Home />);
    expect(screen.getByText('Welcome to APproject')).toBeInTheDocument();
  });

  it('renders the chat widget link', () => {
    render(<Home />);
    const link = screen.getByRole('link', { name: /Try the Chat Widget/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', '/widget');
  });

  it('renders the agent dashboard link', () => {
    render(<Home />);
    const link = screen.getByRole('link', { name: /Agent Dashboard/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', '/dashboard');
  });

  it('renders the Key Features section', () => {
    render(<Home />);
    expect(screen.getByText('Key Features')).toBeInTheDocument();
    expect(screen.getByText('Real-time Chat Widget')).toBeInTheDocument();
    expect(screen.getByText('Agent Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Multi-Tenant Support')).toBeInTheDocument();
  });

  it('renders the Embed the Widget section with code example', () => {
    render(<Home />);
    expect(screen.getByText('Embed the Widget')).toBeInTheDocument();
    expect(screen.getByText(/<div id="app-chat-widget"/i)).toBeInTheDocument();
    expect(screen.getByText(/<script src="http:\/\/localhost:3000\/widget\/bundle.js"/i)).toBeInTheDocument();
  });

  it('renders the Multi-Tenant Demonstration section with tenant links', () => {
    render(<Home />);
    expect(screen.getByText('Multi-Tenant Demonstration')).toBeInTheDocument();
    
    const tenantALink = screen.getByRole('link', { name: /Widget for Tenant A/i });
    expect(tenantALink).toBeInTheDocument();
    expect(tenantALink).toHaveAttribute('href', '/widget?tenantId=tenantA');

    const tenantBLink = screen.getByRole('link', { name: /Widget for Tenant B/i });
    expect(tenantBLink).toBeInTheDocument();
    expect(tenantBLink).toHaveAttribute('href', '/widget?tenantId=tenantB');

    const tenantCLink = screen.getByRole('link', { name: /Widget for Tenant C/i });
    expect(tenantCLink).toBeInTheDocument();
    expect(tenantCLink).toHaveAttribute('href', '/widget?tenantId=tenantC');
  });
});

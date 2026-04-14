import { Footer, Hero, Highlights, Logo, Navbar, Subscribe } from './index';

describe('Main component exports', () => {
  it('exports all shared main components', () => {
    expect(Footer).toBeDefined();
    expect(Hero).toBeDefined();
    expect(Highlights).toBeDefined();
    expect(Logo).toBeDefined();
    expect(Navbar).toBeDefined();
    expect(Subscribe).toBeDefined();
  });
});
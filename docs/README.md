# TNI Synthesizer Suite Documentation

This directory contains the Jekyll-based documentation site for TNI Synthesizer Suite.

## Local Development

### Prerequisites
- Ruby 3.0 or higher
- Bundler gem
- Git

### Setup
```bash
cd docs
bundle install
bundle exec jekyll serve
```

The site will be available at `http://localhost:4000/TNI-1/`

### Development Commands
```bash
# Install dependencies
bundle install

# Serve locally with live reload
bundle exec jekyll serve --livereload

# Build for production
bundle exec jekyll build

# Clean build files
bundle exec jekyll clean
```

## Site Structure

```
docs/
├── _config.yml              # Jekyll configuration
├── Gemfile                  # Ruby dependencies
├── index.md                 # Homepage
├── getting-started.md       # Installation and setup guide
├── user-guide.md           # Complete user documentation
├── weather-api.md          # Weather API integration guide
├── troubleshooting.md      # Common issues and solutions
├── api-reference.md        # Technical API documentation
├── _layouts/               # Custom page layouts
│   └── page.html
├── _includes/              # Reusable components
│   └── breadcrumb.html
└── assets/
    ├── css/
    │   └── style.scss      # Custom styles
    └── images/             # Documentation images
```

## Deployment

The site is automatically deployed to GitHub Pages via GitHub Actions when changes are pushed to the `main` branch.

### Manual Deployment
If needed, you can trigger deployment manually:
1. Go to the repository's Actions tab
2. Select "Build and Deploy Jekyll Documentation"
3. Click "Run workflow"

## Adding New Pages

1. **Create the Markdown file** in the `docs/` directory
2. **Add front matter**:
   ```markdown
   ---
   layout: page
   title: Page Title
   permalink: /page-url/
   nav_order: 5
   ---
   ```
3. **Update navigation** in `_config.yml` if needed
4. **Add to main menu** by adding to `header_pages` list

## Customization

### Styling
- Main styles: `assets/css/style.scss`
- Color scheme defined in CSS custom properties
- Responsive design included
- Dark mode support available

### Layouts
- Page layout: `_layouts/page.html`
- Includes navigation and breadcrumbs
- Customizable per page

### Navigation
- Main navigation configured in `_config.yml`
- Breadcrumb navigation in `_includes/breadcrumb.html`
- Page-to-page navigation automatically generated

## Content Guidelines

### Writing Style
- Use clear, concise language
- Include code examples where helpful
- Provide step-by-step instructions
- Add screenshots for UI elements

### Markdown Features
- Standard Markdown syntax
- Code blocks with syntax highlighting
- Tables for structured data
- Front matter for page configuration

### Code Examples
```java
// Use proper syntax highlighting
public class Example {
    public static void main(String[] args) {
        System.out.println("Hello, TNI Synthesizer!");
    }
}
```

```bash
# Command line examples
mvn clean compile
java -jar tni-synthesizer.jar
```

## SEO and Analytics

### Search Engine Optimization
- SEO tags automatically generated
- Proper page titles and descriptions
- Social media meta tags
- Sitemap automatically generated

### Performance
- Minimized CSS and JavaScript
- Optimized images
- Fast loading times
- Mobile-responsive design

## Troubleshooting Documentation Build

### Common Issues

**Bundle install fails**
```bash
# Update RubyGems
gem update --system
gem install bundler
bundle install
```

**Jekyll serve fails**
```bash
# Clean and rebuild
bundle exec jekyll clean
bundle exec jekyll build
bundle exec jekyll serve
```

**GitHub Pages deployment fails**
- Check GitHub Actions logs
- Verify Jekyll configuration
- Ensure all links are valid

### Validation
```bash
# Check for broken links
bundle exec jekyll build
# Then use a link checker tool

# Validate HTML
bundle exec htmlproofer ./_site
```

## Contributing

1. **Fork the repository**
2. **Create a feature branch**
3. **Make documentation changes**
4. **Test locally**: `bundle exec jekyll serve`
5. **Submit a pull request**

### Review Process
- All changes are reviewed before merging
- Check for spelling and grammar
- Verify all links work
- Test on multiple devices/browsers

---

For technical questions about the documentation system, please open an issue in the main repository.
# Contributing to Fintech Ledger System

## Development Setup

1. Clone the repository
2. Ensure Java 21 is installed
3. Run `./gradlew build` to build the project
4. Start infrastructure: `make start`
5. Run tests: `./gradlew test`

## Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add Javadoc for public APIs
- Keep methods focused and small

## Testing

- Write tests for new features
- Ensure all tests pass before submitting PR
- Aim for 80%+ code coverage on new code

## Commit Messages

Use clear, descriptive commit messages:
- `feat: Add transaction limit service`
- `fix: Resolve idempotency key validation issue`
- `docs: Update API documentation`

## Pull Request Process

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure CI passes
5. Submit PR with description
6. Address review comments


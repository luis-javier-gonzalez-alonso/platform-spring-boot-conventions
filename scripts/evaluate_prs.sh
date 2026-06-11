#!/bin/bash
set -e

# Fetch all open dependabot PRs
PRS=$(gh pr list --state open --author "app/dependabot" --json number -q '.[].number')

for PR in $PRS; do
    echo "======================================"
    echo "Evaluating PR #$PR"
    echo "======================================"
    
    # Checkout PR
    gh pr checkout $PR
    
    # Evaluate PR (run build and test)
    if ./gradlew clean build; then
        echo "Build succeeded for PR #$PR. Merging..."
        # Switch back to main to avoid issues when dependabot deletes the branch
        git checkout main
        gh pr merge $PR --squash --delete-branch
        git pull origin main
    else
        echo "Build failed for PR #$PR. Skipping."
        git checkout main
        git pull origin main
    fi
done

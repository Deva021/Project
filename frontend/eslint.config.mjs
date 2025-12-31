import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";
import globals from "globals"; // Import globals
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
    // Ignore integration test files due to persistent parsing errors
    "__tests__/integration/session-persistence.test.ts",
    "__tests__/integration/signin.test.ts",
    "__tests__/integration/signout.test.ts",
  ]),
  {
    files: ["**/*.test.ts", "**/*.test.tsx"],
    languageOptions: {
      globals: {
        ...globals.jest, // Add Jest globals
      },
      parserOptions: {
        project: [path.join(__dirname, './tsconfig.json')], // Specify tsconfig for project-wide type checking
        tsconfigRootDir: __dirname, // Root directory of tsconfig.json
      },
    },
    rules: {
      // Potentially add Jest-specific rules or disable conflicting ones
    }
  }
]);

export default eslintConfig;

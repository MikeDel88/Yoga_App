import { describe, expect, it } from '@jest/globals';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Vérifie que la suite de tests respecte la pyramide de tests définie pour
 * ce projet : au moins 30% des tests (`it`/`it.each`/`test`/`test.each`)
 * doivent être des tests d'intégration. La convention du projet est de
 * regrouper les tests d'intégration dans un `describe` imbriqué dont le
 * titre contient "Tests d'intégration" (voir login.component.spec.ts,
 * register.component.spec.ts, detail.component.spec.ts,
 * form.component.spec.ts, list.component.spec.ts, me.component.spec.ts).
 * Équivalent front de back/src/test/java/.../TestPyramidRatioTest.java.
 */

const MINIMUM_INTEGRATION_RATIO = 0.3;
const SELF_FILENAME = 'test-pyramid-ratio.spec.ts';
const SRC_ROOT = path.join(__dirname);

const TEST_CALL_PATTERN = /\b(?:it|test)(?:\.each)?\s*\(/g;
const DESCRIBE_PATTERN = /\bdescribe\s*\(\s*(['"`])((?:\\.|(?!\1).)*)\1/g;
const INTEGRATION_TITLE_PATTERN = /Tests d.int[ée]gration/i;

function findSpecFiles(dir: string): string[] {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  const files: string[] = [];

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...findSpecFiles(fullPath));
    } else if (entry.name.endsWith('.spec.ts') && entry.name !== SELF_FILENAME) {
      files.push(fullPath);
    }
  }

  return files;
}

/**
 * Repère, pour un fichier donné, les intervalles de caractères qui se
 * trouvent à l'intérieur d'un `describe(...)` d'intégration, en suivant la
 * profondeur d'accolades `{`/`}` pour savoir quand le bloc se referme.
 */
function findIntegrationRanges(content: string): Array<[number, number]> {
  const describeStarts: Array<{ index: number; isIntegration: boolean }> = [];
  DESCRIBE_PATTERN.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = DESCRIBE_PATTERN.exec(content)) !== null) {
    describeStarts.push({
      index: match.index,
      isIntegration: INTEGRATION_TITLE_PATTERN.test(match[2]),
    });
  }

  const ranges: Array<[number, number]> = [];
  let depth = 0;
  let describePointer = 0;
  const openStack: Array<{ depthAtOpen: number; isIntegration: boolean; start: number }> = [];

  for (let i = 0; i < content.length; i++) {
    while (
      describePointer < describeStarts.length &&
      describeStarts[describePointer].index === i
    ) {
      const { isIntegration } = describeStarts[describePointer];
      const braceIndex = content.indexOf('{', i);
      if (braceIndex !== -1) {
        openStack.push({ depthAtOpen: depth, isIntegration, start: braceIndex });
      }
      describePointer++;
    }

    const char = content[i];
    if (char === '{') {
      depth++;
    } else if (char === '}') {
      depth--;
      const top = openStack[openStack.length - 1];
      if (top && depth === top.depthAtOpen) {
        openStack.pop();
        if (top.isIntegration) {
          ranges.push([top.start, i]);
        }
      }
    }
  }

  return ranges;
}

function isWithinRanges(index: number, ranges: Array<[number, number]>): boolean {
  return ranges.some(([start, end]) => index > start && index < end);
}

function countTests(content: string): { total: number; integration: number } {
  const integrationRanges = findIntegrationRanges(content);
  let total = 0;
  let integration = 0;

  TEST_CALL_PATTERN.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = TEST_CALL_PATTERN.exec(content)) !== null) {
    total++;
    if (isWithinRanges(match.index, integrationRanges)) {
      integration++;
    }
  }

  return { total, integration };
}

describe('Test pyramid', () => {
  it('should have at least 30% of tests classified as integration tests', () => {
    const specFiles = findSpecFiles(SRC_ROOT);

    let totalTests = 0;
    let integrationTests = 0;

    for (const file of specFiles) {
      const content = fs.readFileSync(file, 'utf-8');
      const { total, integration } = countTests(content);
      totalTests += total;
      integrationTests += integration;
    }

    const ratio = integrationTests / totalTests;

    // eslint-disable-next-line no-console
    console.log(
      `Integration test ratio: ${integrationTests}/${totalTests} = ${(ratio * 100).toFixed(1)}% ` +
        `(minimum required: ${(MINIMUM_INTEGRATION_RATIO * 100).toFixed(0)}%)`
    );

    expect(ratio).toBeGreaterThanOrEqual(MINIMUM_INTEGRATION_RATIO);
  });
});

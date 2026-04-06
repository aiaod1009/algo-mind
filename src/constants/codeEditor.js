export const DEFAULT_EDITOR_LANGUAGE = 'java'

export const LANGUAGE_OPTIONS = [
  { label: 'Java', value: 'java' },
  { label: 'C++', value: 'cpp' },
  { label: 'Python', value: 'python' },
  { label: 'JavaScript', value: 'js' },
]

export const LANGUAGE_LABEL_MAP = {
  java: 'Java',
  cpp: 'C++',
  python: 'Python',
  js: 'JavaScript',
}

export const CODE_TEMPLATE_MAP = {
  java: ({ prompt = 'Write your solution here.' } = {}) => `import java.util.*;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    // ${prompt}
  }
}
`,
  cpp: ({ prompt = 'Write your solution here.' } = {}) => `#include <iostream>
#include <vector>
using namespace std;

int main() {
  ios::sync_with_stdio(false);
  cin.tie(nullptr);

  // ${prompt}
  return 0;
}
`,
  python: ({ prompt = 'Write your solution here.' } = {}) => `def solve():
    # ${prompt}
    pass


if __name__ == "__main__":
    solve()
`,
  js: ({ prompt = 'Write your solution here.' } = {}) => `function solve() {
  // ${prompt}
}

solve()
`,
}

export const getCodeTemplate = (language = DEFAULT_EDITOR_LANGUAGE, prompt) => {
  const createTemplate = CODE_TEMPLATE_MAP[language] || CODE_TEMPLATE_MAP[DEFAULT_EDITOR_LANGUAGE]
  return createTemplate({ prompt })
}

export const isTemplateLikeCode = (value, prompt) => {
  const normalizedValue = String(value || '').trim()
  if (!normalizedValue) {
    return true
  }

  return Object.keys(CODE_TEMPLATE_MAP).some((language) => {
    return getCodeTemplate(language, prompt).trim() === normalizedValue
  })
}

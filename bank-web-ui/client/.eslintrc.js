// All rules explained here: http://eslint.org/docs/rules/
module.exports = {
  plugins: ["vue"],
  rules: {
    'no-console': 0, // Allow console.* on browsers
    'no-invalid-this': 0, // Used extensively on vue components
    'vue/jsx-uses-vars': 2,
  },
  env: {
    es6: true,
    browser: true,
    commonjs: true,
    node: false, // Disable node env for browsers.  NOTE: This doesn't work yet, will be fixed in future eslint version.
  },
  parserOptions: {
    "sourceType": "module"
  },
};

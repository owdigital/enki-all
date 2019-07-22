// All rules explained here: http://eslint.org/docs/rules/
// Basically:
//   "rule-name": 0 // disable rule
//   "rule-name": 1 // enable rule as a warning (ie. warns you, but keeps going)
//   "rule-name": 2 // enable rule as an error (shows error & stops right there)
// Also:
//   "rule-name": [2, 'always'] // enable rule as error, with the option 'always'
//   "rule-name": [2, { boolean: false, awesome: true }] // enable rule as error, with an object for the options

// NOTE: Some of the rules below are commented out.  We should discuss these as a team, and figure out which ones we want to enable as the default ow-labs ruleset.  Then we'll remove the rest.
module.exports = {
    rules: {

        // visual formatting //
        'indent': [1, 2],  // warnings-only, 2-space indents.
        'semi': [2, "always"],

        // Prevent bugs //
        'no-dupe-args': 2,
        'no-dupe-keys': 2,
        'no-duplicate-case': 2,
        'no-empty-character-class': 2,
        'no-empty': 2,
        'no-extra-boolean-cast': 2,
        'no-extra-parens': 2,
        'no-extra-semi': 2,
        'no-func-assign': 2,
        'no-inner-declarations': 2,
        'no-invalid-regexp': 2,
        'no-irregular-whitespace': 2,
        'no-obj-calls': 2,
        'no-sparse-arrays': 2,
        'no-unexpected-multiline': 2,
        'no-unreachable': 2,
        'use-isnan': 2,
        'valid-jsdoc': 2,
        'valid-typeof': 2,
        'curly': [2, "multi-line"],
        'dot-location': [2, "property"],
        'dot-notation': 2,
        'eqeqeq': [2, 'allow-null'],
        'no-caller': 2,
        'no-case-declarations': 2,
        'no-labels': 2,
        'no-extra-bind': 2,
        'no-fallthrough': 2,
        "no-implicit-coercion": [2, { boolean: false, number: true, string: true }],
        'no-implied-eval': 2,
        'no-invalid-this': 2,
        'no-iterator': 2,
        'no-labels': 2,
        'no-lone-blocks': 2,
        'no-multi-spaces': 2,
        'no-native-reassign': 2,
        'no-new-func': 2,
        'no-new-wrappers': 2,
        'no-new': 2,
        'no-octal-escape': 2,
        'no-octal': 2,
        'no-proto': 2,
        'no-redeclare': 2,
        'no-return-assign': 2,
        'no-self-compare': 2,
        'no-sequences': 2,
        'no-throw-literal': 2,
        'no-unused-expressions': 2,
        'no-useless-call': 2,
        'no-useless-concat': 2,
        'no-void': 2,
        'radix': 2,
        "wrap-iife": [2, "any"],
        'no-catch-shadow': 2,
        'no-delete-var': 2,
        'no-label-var': 2,
        'no-shadow-restricted-names': 2,
        'no-shadow': 2,
        'no-undef-init': 2,
        'no-undef': 2,
        'no-unused-vars': [2, { args: 'after-used' }],
        'no-use-before-define': [2, 'nofunc'],

        // Node-specific //
        'global-require': 2,
        'handle-callback-err': 2,
        'no-mixed-requires': 2,
        'no-new-require': 2,
        'no-path-concat': 2,
        
        'no-console': 2,  // Should use winston instead for all server-side code.
    },
    env: {
        node: true,
        es6: true
    },
    globals: {
        logger: false, // "logger" is our global winston instance.  "false" means code can't overwrite it.
        koe: false
    },

    parser: "babel-eslint",
    parserOptions: {
        sourceType: "module",
    },
};

/**
 * @type {Cypress.PluginConfig}
 */
import registerCodeCoverageTasks from '@cypress/code-coverage/task';

 export default (on, config) => {
   return registerCodeCoverageTasks(on, config);
 };

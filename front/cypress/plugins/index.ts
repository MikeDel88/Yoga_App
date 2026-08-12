/**
 * @type {Cypress.PluginConfig}
 */
import registerCodeCoverageTasks from '@cypress/code-coverage/task';

 export default (on: Cypress.PluginEvents, config: Cypress.PluginConfigOptions) => {
   return registerCodeCoverageTasks(on, config);
 };

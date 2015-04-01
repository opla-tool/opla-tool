/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.algorithms;

import arquitetura.io.ReaderConfig;
import com.ufpr.br.opla.utils.MutationOperatorsSelected;
import com.ufpr.br.opla.configuration.UserHome;
import com.ufpr.br.opla.configuration.VolatileConfs;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jmetal.experiments.*;

/**
 *
 * @author elf
 */
public class PAES {
    
    public void execute(JComboBox comboAlgorithms, JCheckBox checkMutation, JTextField fieldMutationProb,
            JTextArea fieldArchitectureInput, JTextField fieldNumberOfRuns, JTextField fieldPaesArchiveSize,
            JTextField fieldMaxEvaluations, JCheckBox checkCrossover, JTextField fieldCrossoverProbability, String executionDescription) {
        
        ReaderConfig.setPathToConfigurationFile(UserHome.getPathToConfigFile());
        ReaderConfig.load();
        
        PaesConfigs configs = new PaesConfigs();
        configs.setDescription(executionDescription);

        //Se mutação estiver marcada, pega os operadores selecionados
        //,e seta a probabilidade de mutacao
        if (checkMutation.isSelected()) {
            List<String> mutationsOperators = MutationOperatorsSelected.getSelectedMutationOperators();
            configs.setMutationOperators(mutationsOperators);
            configs.setMutationProbability(Double.parseDouble(fieldMutationProb.getText()));
        }
        
        configs.setPlas(fieldArchitectureInput.getText());
        configs.setNumberOfRuns(Integer.parseInt(fieldNumberOfRuns.getText()));
        configs.setMaxEvaluations(Integer.parseInt(fieldMaxEvaluations.getText()));
        configs.setArchiveSize(Integer.parseInt(fieldPaesArchiveSize.getText()));
               

        //Se crossover estiver marcado, configura probabilidade
        //Caso contrario desativa
        if (checkCrossover.isSelected()) {
            configs.setCrossoverProbability(Double.parseDouble(fieldCrossoverProbability.getText()));
        } else {
            configs.disableCrossover();            
        }
        
        //OPA-Patterns Configurations
        if (MutationOperatorsSelected.getSelectedMutationOperators().contains(FeatureMutationOperators.DESIGN_PATTERNS.getOperatorName())) {
          String[] array = new String[MutationOperatorsSelected.getSelectedPatternsToApply().size()];
          configs.setPatterns(MutationOperatorsSelected.getSelectedPatternsToApply().toArray(array));
         configs.setDesignPatternStrategy(VolatileConfs.getScopePatterns());
        }

        //Configura onde o db esta localizado
        configs.setPathToDb(UserHome.getPathToDb());

        //Instancia a classe de configuracao da OPLA.java
        OPLAConfigs oplaConfig = new OPLAConfigs();
        
      
        oplaConfig.setSelectedObjectiveFunctions(VolatileConfs.getObjectiveFunctionSelected());

        //Add as confs de OPLA na classe de configuracoes gerais.
        configs.setOplaConfigs(oplaConfig);

        //Utiliza a classe Initializer do NSGAII passando as configs.
        PAES_OPLA_FeatMutInitializer paes = new PAES_OPLA_FeatMutInitializer(configs);

        //Executa
        paes.run();
        
    }
    
    
}

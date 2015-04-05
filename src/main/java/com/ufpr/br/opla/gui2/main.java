/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.gui2;

import arquitetura.helpers.LogConfiguration;
import br.ufpr.inf.opla.patterns.strategies.scopeselection.impl.ElementsWithSameDesignPatternSelection;
import com.ufpr.br.opla.algorithms.NSGAII;
import com.ufpr.br.opla.algorithms.PAES;
import com.ufpr.br.opla.algorithms.Solution;
import com.ufpr.br.opla.charts.ChartGenerate;
import com.ufpr.br.opla.charts.EdBar;
import com.ufpr.br.opla.charts.EdLine;
import com.ufpr.br.opla.configuration.*;
import com.ufpr.br.opla.logs.LogListener;
import com.ufpr.br.opla.utils.*;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import jmetal.experiments.FeatureMutationOperators;
import jmetal.experiments.Metrics;
import logs.log_log.Level;
import logs.log_log.Logger;
import metrics.Conventional;
import metrics.Elegance;
import metrics.FeatureDriven;
import metrics.PLAExtensibility;
import net.miginfocom.swing.MigLayout;
import results.Execution;

/**
 *
 * @author elf
 */
public class main extends javax.swing.JFrame {

  private ManagerApplicationConfig config = null;
  private String pathSmartyBck;
  private String pathConcernBck;
  private String pathRelationshipsBck;
  private String pathPatternsBck;
  private String crossoverProbabilityBck;
  private String selectedExperiment;
  private String selectedExecution;
  private ElementsWithSameDesignPatternSelection ewsdp = null;
  private JTextArea textLogsArea = new javax.swing.JTextArea();
  Locale locale = Locale.US;
  ResourceBundle rb = ResourceBundle.getBundle("i18n", locale);

  /**
   * Creates new form main
   */
  public main() throws Exception {

    DefaultCaret caret = (DefaultCaret) textLogsArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    Logger.addListener(new LogListener(textLogsArea));
    Logger.getLogger().putLog("Inicializando OPLA-Tool");

    LogConfiguration.setLogLevel(org.apache.log4j.Level.OFF);

    Utils.createPathsOplaTool();

    config = ApplicationFile.getInstance();
    GuiServices guiservices = new GuiServices(config);
    guiservices.copyFileGuiSettings();
    GuiUtils.fontSize(GuiFile.getInstance().getFontSize());

    initComponents();

    configureDb();
    initAlgorithmsCombo();
    disableFieldsOnStart();
    checkAllMutationOperatorsByDefault();
    hidePanelMutationOperatorsByDefault();
    hidePanelCrossoverProbabilityByDefault();
    hidePanelMutationProbabilityByDefault();
    hidePanelSolutionsByDefault();
    hidePanelShowMetricsByDefault();
    checkAllMetricsByDefault();
    initiExecutedExperiments();
    btnShowConfigurations.setEnabled(false);

    VolatileConfs.configureDefaultPatternScope();

    panelExecutions.setVisible(false);
    disactiveTabFinalizedWhenNotExperimentsFound();

    guiservices.configureSmartyProfile(fieldSmartyProfile, checkSmarty, btnSmartyProfile);
    guiservices.configureConcernsProfile(fieldConcernProfile, checkConcerns, btnConcernProfile);
    guiservices.configurePatternsProfile(fieldPatternsProfile, checkPatterns, btnPatternProfile);
    guiservices.configureRelationshipsProfile(fieldRelationshipsProfile, checkRelationships, btnRelationshipProfile);
    guiservices.configureTemplates(fieldTemplate);
    guiservices.configureLocaleToSaveModels(fieldManipulationDir);
    guiservices.configureLocaleToExportModels(fieldOutput);
    guiservices.copyBinHypervolume();

    activeFieldsAndChecks();
    guiservices.hidePanelPatternScopeByDefault(panelPatternScope);
  }

  private void activeFieldsAndChecks() {
    fieldSmartyProfile.setEditable(false);
    fieldConcernProfile.setEditable(false);
    fieldPatternsProfile.setEditable(false);
    fieldRelationshipsProfile.setEditable(false);

    if (fieldSmartyProfile.getText().isEmpty()) {
      checkSmarty.setSelected(false);
    } else {
      checkSmarty.setSelected(true);
    }

    if (fieldConcernProfile.getText().isEmpty()) {
      checkConcerns.setSelected(false);
    } else {
      checkConcerns.setSelected(true);
    }

    if (fieldPatternsProfile.getText().isEmpty()) {
      checkPatterns.setSelected(false);
    } else {
      checkPatterns.setSelected(true);
    }

    if (fieldRelationshipsProfile.getText().isEmpty()) {
      checkRelationships.setSelected(false);
    } else {
      checkRelationships.setSelected(true);
    }
  }

  private void addFlagOperatorDesignPatternUsed() {
    if (!MutationOperatorsSelected.getSelectedMutationOperators().contains(FeatureMutationOperators.DESIGN_PATTERNS.getOperatorName())) {
      MutationOperatorsSelected.getSelectedMutationOperators().add(FeatureMutationOperators.DESIGN_PATTERNS.getOperatorName());

      panelPatternScope.setVisible(true);

    } else if (noneDesignPatternsSelected()) {
      MutationOperatorsSelected.getSelectedMutationOperators().remove(FeatureMutationOperators.DESIGN_PATTERNS.getOperatorName());

      panelPatternScope.setVisible(false);
    }
  }

  private void disactiveTabFinalizedWhenNotExperimentsFound() {
    if (db.Database.getContent().isEmpty()) {
      jTabbedPane1.setEnabledAt(3, false);
    }
  }

  private void addOrRemoveOperatorMutation(final String operatorName, JCheckBox check) {
    if (!check.isSelected()) {
      MutationOperatorsSelected.getSelectedMutationOperators().remove(operatorName);
    } else {
      MutationOperatorsSelected.getSelectedMutationOperators().add(operatorName);
    }
  }

  private void addOrRemovePatternToApply(final String patternName, JCheckBox check) {
    if (!check.isSelected()) {
      MutationOperatorsSelected.getSelectedPatternsToApply().remove(patternName);
    } else {
      MutationOperatorsSelected.getSelectedPatternsToApply().add(patternName);
    }
  }

  private void addToMetrics(JCheckBox check, final String metric) {
    if (check.isSelected()) {
      VolatileConfs.getObjectiveFunctionSelected().add(metric);
    } else {
      VolatileConfs.getObjectiveFunctionSelected().remove(metric);
    }
  }

  private void createTableExecutions(String idExperiment) throws HeadlessException {
    panelExecutions.setVisible(true);
    DefaultTableModel modelTableExecutions = new DefaultTableModel();
    modelTableExecutions.addColumn("Run");
    modelTableExecutions.addColumn("Time (min:seg)");
    modelTableExecutions.addColumn("Genr. Solutions");
    modelTableExecutions.addColumn("Non Dominated Solutions");
    tableExecutions.setModel(modelTableExecutions);

    GuiUtils.makeTableNotEditable(tableExecutions);

    int rowIndex = tableExp.getSelectedRow();
    String currentExperimentId = tableExp.getModel().getValueAt(rowIndex, 0).toString();
    

    
    int  numberNonDominatedSolutions = db.Database.countNumberNonDominatedSolutins(idExperiment);
    

    try {
      Collection<Execution> all = db.Database.getAllExecutionsByExperimentId(idExperiment);
      for (Execution exec : all) {
        Object[] row = new Object[5];
        row[0] = exec.getId();
        row[1] = Time.convertMsToMin(exec.getTime());
        // int numberNonDominatedSolutions = ReadSolutionsFiles.countNumberNonDominatedSolutins(idExperiment, this.config.getConfig().getDirectoryToExportModels());

        int numberSolutions = db.Database.getAllSolutionsForExecution(idExperiment, exec.getId()).size();

//        int numberSolutions = ReadSolutionsFiles.read(idExperiment,
//                exec.getId(),
//                this.config.getConfig().getDirectoryToExportModels()).size();

        row[2] = Math.abs(numberSolutions - numberNonDominatedSolutions);
        row[3] = numberNonDominatedSolutions;
        modelTableExecutions.addRow(row);
      }
      all.clear();
    } catch (Exception e) {
      //
    }
  }

  private void executeNSGAII() {
    try {
      NSGAII nsgaii = new NSGAII();
      nsgaii.execute(comboAlgorithms, checkMutation, fieldMutationProb,
              fieldArchitectureInput, fieldNumberOfRuns, fieldPopulationSize,
              fieldMaxEvaluations, checkCrossover,
              fieldCrossoverProbability, executionDescription.getText());


    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error when try execute NSGA-II, Finalizing...." + e.getMessage());
      Logger.getLogger().putLog(String.format("Error when try execute NSGA-II, Finalizing...",
              Level.FATAL, main.class.getName()));
      System.exit(1);
    }
  }

  private void executePAES() {
    try {
      PAES paes = new PAES();
      Logger.getLogger().putLog("Execution NSGAII...");
      jTabbedPane1.setSelectedIndex(4);
      paes.execute(comboAlgorithms, checkMutation, fieldMutationProb,
              fieldArchitectureInput, fieldNumberOfRuns, fieldPaesArchiveSize,
              fieldMaxEvaluations, checkCrossover,
              fieldCrossoverProbability, executionDescription.getText());


    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error when try execute PAES Finalizing...." + e.getMessage());
      Logger.getLogger().putLog(String.format("Error when try execute PAES, Finalizing...",
              Level.FATAL, main.class.getName()));
      System.exit(1);
    }
  }

  private void initAlgorithmsCombo() {
    String algoritms[] = {"Select One", "NSGA-II", "PAES"};
    comboAlgorithms.removeAllItems();

    for (int i = 0; i < algoritms.length; i++) {
      comboAlgorithms.addItem(algoritms[i]);
    }

    comboAlgorithms.setSelectedIndex(0);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        ApplicationConfs = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        checkSmarty = new javax.swing.JCheckBox();
        checkConcerns = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        fieldSmartyProfile = new javax.swing.JTextField();
        btnSmartyProfile = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        fieldConcernProfile = new javax.swing.JTextField();
        btnConcernProfile = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        fieldPatternsProfile = new javax.swing.JTextField();
        btnPatternProfile = new javax.swing.JButton();
        checkPatterns = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        fieldRelationshipsProfile = new javax.swing.JTextField();
        btnRelationshipProfile = new javax.swing.JButton();
        checkRelationships = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        fieldTemplate = new javax.swing.JTextField();
        btnTemplate = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        fieldManipulationDir = new javax.swing.JTextField();
        btnManipulationDir = new javax.swing.JButton();
        algorithms = new javax.swing.JPanel();
        panelExperimentSettings = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        fieldNumberOfRuns = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        fieldMaxEvaluations = new javax.swing.JTextField();
        panelCrossProb = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        fieldCrossoverProbability = new javax.swing.JTextField();
        crossProbSlider = new javax.swing.JSlider();
        panelMutationProb = new javax.swing.JPanel();
        mutatinProbSlider = new javax.swing.JSlider();
        fieldMutationProb = new javax.swing.JTextField();
        checkMutation = new javax.swing.JCheckBox();
        labelOperators = new javax.swing.JLabel();
        checkCrossover = new javax.swing.JCheckBox();
        comboAlgorithms = new javax.swing.JComboBox();
        labelAlgorithms = new javax.swing.JLabel();
        panelMetrics = new javax.swing.JPanel();
        checkConventional = new javax.swing.JCheckBox();
        checkElegance = new javax.swing.JCheckBox();
        checkPLAExt = new javax.swing.JCheckBox();
        checkFeatureDriven = new javax.swing.JCheckBox();
        panelOperatorsMutation = new javax.swing.JPanel();
        checkFeatureMutation = new javax.swing.JCheckBox();
        checkMoveMethod = new javax.swing.JCheckBox();
        checkMoveOperation = new javax.swing.JCheckBox();
        checkManagerClass = new javax.swing.JCheckBox();
        checkMoveAttribute = new javax.swing.JCheckBox();
        checkAddClass = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        fieldPopulationSize = new javax.swing.JTextField();
        labelArchivePAES = new javax.swing.JLabel();
        fieldPaesArchiveSize = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fieldArchitectureInput = new javax.swing.JTextArea();
        btnCleanListArchs1 = new javax.swing.JButton();
        btnInput1 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        fieldOutput = new javax.swing.JTextField();
        btnOutput = new javax.swing.JButton();
        btnRun = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        executionDescription = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        checkMediator = new javax.swing.JCheckBox();
        checkStrategy = new javax.swing.JCheckBox();
        checkBridge = new javax.swing.JCheckBox();
        panelPatternScope = new javax.swing.JPanel();
        radioRandomStrategy = new javax.swing.JRadioButton();
        radioElementsWithSameDPorNone = new javax.swing.JRadioButton();
        experiments = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableExp = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        panelExecutions = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableExecutions = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        panelSolutions = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        comboSolutions = new javax.swing.JComboBox();
        panelObjectives = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableObjectives = new javax.swing.JTable();
        comboMetrics = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        panelShowMetrics = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableMetrics = new javax.swing.JTable();
        bestSolutions = new javax.swing.JButton();
        btnShowConfigurations = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tableExp2 = new javax.swing.JTable();
        panelFunctionExecutionsSelecteds = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        btnGenerateChart = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        btnHypervolume = new javax.swing.JButton();
        hypervolumeNormalized = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        btnGenerateEdChart = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();

        jLabel12 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OPLA-Tool 0.0.1");

        //jTabbedPane1.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jButton1.setText("Visualize your application.yaml file");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Profiles Configuration", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        checkSmarty.setText("SMarty");
        checkSmarty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkSmartyActionPerformed(evt);
            }
        });

        checkConcerns.setText("Feature");
        checkConcerns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkConcernsActionPerformed(evt);
            }
        });

        jLabel1.setText("SMarty Profile:");

        fieldSmartyProfile.setName("pathToSmarty"); // NOI18N
        fieldSmartyProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldSmartyProfileActionPerformed(evt);
            }
        });

        btnSmartyProfile.setText("Browser...");
        btnSmartyProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmartyProfileActionPerformed(evt);
            }
        });

        jLabel2.setText("Feature Profile:");

        fieldConcernProfile.setName("sdfs"); // NOI18N
        fieldConcernProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldConcernProfileActionPerformed(evt);
            }
        });

        btnConcernProfile.setText("Browser...");
        btnConcernProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConcernProfileActionPerformed(evt);
            }
        });

        jLabel10.setText("Patterns Profile:");

        btnPatternProfile.setText("Browser...");
        btnPatternProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPatternProfileActionPerformed(evt);
            }
        });

        checkPatterns.setText("Patterns");
        checkPatterns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkPatternsActionPerformed(evt);
            }
        });

        jLabel18.setText("Relationships Profile:");

        btnRelationshipProfile.setText("Browser...");
        btnRelationshipProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelationshipProfileActionPerformed(evt);
            }
        });

        checkRelationships.setText("Relationships");
        checkRelationships.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRelationshipsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(fieldSmartyProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSmartyProfile))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(checkSmarty)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkConcerns)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkPatterns)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkRelationships))
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel10)
                    .addComponent(jLabel18)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(fieldRelationshipsProfile, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fieldPatternsProfile, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fieldConcernProfile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnConcernProfile)
                            .addComponent(btnPatternProfile)
                            .addComponent(btnRelationshipProfile, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkSmarty)
                    .addComponent(checkConcerns)
                    .addComponent(checkPatterns)
                    .addComponent(checkRelationships))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldSmartyProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSmartyProfile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldConcernProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConcernProfile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldPatternsProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPatternProfile))
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldRelationshipsProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRelationshipProfile))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Template Configuration", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel7.setText("about templates");
        jLabel7.setToolTipText("<html><h3>About Templtes</h3><br/>\n\nTexto explicando brevemente o que são os templates e para que servem.");

        jLabel8.setText(" Directory:");

        btnTemplate.setText("Select a Directory...");
        btnTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTemplateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(fieldTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTemplate))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manipulation Directory", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel14.setText("about manipulation directory");

        jLabel15.setText("Directory");

        btnManipulationDir.setText("Select a Directory...");
        btnManipulationDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManipulationDirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel14))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(fieldManipulationDir, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnManipulationDir)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel15)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldManipulationDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManipulationDir))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ApplicationConfsLayout = new javax.swing.GroupLayout(ApplicationConfs);
        ApplicationConfs.setLayout(ApplicationConfsLayout);
        ApplicationConfsLayout.setHorizontalGroup(
            ApplicationConfsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ApplicationConfsLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(ApplicationConfsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(390, Short.MAX_VALUE))
        );
        ApplicationConfsLayout.setVerticalGroup(
            ApplicationConfsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ApplicationConfsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jButton1)
                .addGap(29, 29, 29))
        );

        jTabbedPane1.addTab("General Configurations", ApplicationConfs);

        algorithms.setName("algorithms");

        panelExperimentSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Settings", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        jLabel3.setText("Number of Runs:");

        fieldNumberOfRuns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldNumberOfRunsActionPerformed(evt);
            }
        });
        fieldNumberOfRuns.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                numberOfRunsFocusLost(evt);
            }
        });
        fieldNumberOfRuns.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumberOfRunsKeyTyped(evt);
            }
        });

        jLabel4.setText("Max Evaluations:");

        fieldMaxEvaluations.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldMaxEvaluationsFocusLost(evt);
            }
        });
        fieldMaxEvaluations.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldMaxEvaluationsKeyTyped(evt);
            }
        });

        panelCrossProb.setBorder(javax.swing.BorderFactory.createTitledBorder("Crossover Probability"));

        fieldCrossoverProbability.setText("1.0");
        fieldCrossoverProbability.setEnabled(false);

        crossProbSlider.setMajorTickSpacing(1);
        crossProbSlider.setMaximum(10);
        crossProbSlider.setMinimum(1);
        crossProbSlider.setMinorTickSpacing(1);
        crossProbSlider.setPaintLabels(true);
        crossProbSlider.setPaintTicks(true);
        crossProbSlider.setSnapToTicks(true);
        crossProbSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                crossProbSliderMouseDragged(evt);
            }
        });

        javax.swing.GroupLayout panelCrossProbLayout = new javax.swing.GroupLayout(panelCrossProb);
        panelCrossProb.setLayout(panelCrossProbLayout);
        panelCrossProbLayout.setHorizontalGroup(
            panelCrossProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCrossProbLayout.createSequentialGroup()
                .addComponent(crossProbSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(fieldCrossoverProbability, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(151, 151, 151))
        );
        panelCrossProbLayout.setVerticalGroup(
            panelCrossProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCrossProbLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCrossProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(crossProbSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCrossProbLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fieldCrossoverProbability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelMutationProb.setBorder(javax.swing.BorderFactory.createTitledBorder("Mutation Probability"));

        mutatinProbSlider.setMajorTickSpacing(1);
        mutatinProbSlider.setMaximum(10);
        mutatinProbSlider.setMinimum(1);
        mutatinProbSlider.setMinorTickSpacing(1);
        mutatinProbSlider.setPaintLabels(true);
        mutatinProbSlider.setPaintTicks(true);
        mutatinProbSlider.setSnapToTicks(true);
        mutatinProbSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mutatinProbSliderMouseDragged(evt);
            }
        });

        fieldMutationProb.setText("1.0");
        fieldMutationProb.setEnabled(false);

        javax.swing.GroupLayout panelMutationProbLayout = new javax.swing.GroupLayout(panelMutationProb);
        panelMutationProb.setLayout(panelMutationProbLayout);
        panelMutationProbLayout.setHorizontalGroup(
            panelMutationProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMutationProbLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mutatinProbSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldMutationProb, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMutationProbLayout.setVerticalGroup(
            panelMutationProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMutationProbLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(panelMutationProbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mutatinProbSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMutationProbLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(fieldMutationProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkMutation.setText("Mutation");
        checkMutation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMutationActionPerformed(evt);
            }
        });

        labelOperators.setText("Select operators which want to use");

        checkCrossover.setText("Crossover");
        checkCrossover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCrossoverActionPerformed(evt);
            }
        });

        comboAlgorithms.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboAlgorithms.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboAlgorithmsItemStateChanged(evt);
            }
        });
        comboAlgorithms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboAlgorithmsActionPerformed(evt);
            }
        });

        labelAlgorithms.setText("Select algorithm which want to use:");

        panelMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Objective Functions", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        checkConventional.setText("Conventional");
        checkConventional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkConventionalActionPerformed(evt);
            }
        });

        checkElegance.setText("Elegance");
        checkElegance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEleganceActionPerformed(evt);
            }
        });

        checkPLAExt.setText("PLA Extensibility");
        checkPLAExt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkPLAExtActionPerformed(evt);
            }
        });

        checkFeatureDriven.setText("Feature Driven");
        checkFeatureDriven.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFeatureDrivenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMetricsLayout = new javax.swing.GroupLayout(panelMetrics);
        panelMetrics.setLayout(panelMetricsLayout);
        panelMetricsLayout.setHorizontalGroup(
            panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMetricsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkConventional)
                    .addComponent(checkElegance))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkFeatureDriven)
                    .addComponent(checkPLAExt))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        panelMetricsLayout.setVerticalGroup(
            panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMetricsLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkConventional)
                    .addComponent(checkPLAExt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkElegance)
                    .addComponent(checkFeatureDriven))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        panelOperatorsMutation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Mutation Operators wich want to use", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        checkFeatureMutation.setText("Feature-driven Mutation");
        checkFeatureMutation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFeatureMutationActionPerformed(evt);
            }
        });

        checkMoveMethod.setText("Move Method Mutation");
        checkMoveMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMoveMethodActionPerformed(evt);
            }
        });

        checkMoveOperation.setText("Move Operation Mutation");
        checkMoveOperation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMoveOperationActionPerformed(evt);
            }
        });

        checkManagerClass.setText("Add Manager Class Mutation");
        checkManagerClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkManagerClassActionPerformed(evt);
            }
        });

        checkMoveAttribute.setText("Move Attribute Mutation");
        checkMoveAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMoveAttributeActionPerformed(evt);
            }
        });

        checkAddClass.setText("Add Class Mutation");
        checkAddClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAddClassActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOperatorsMutationLayout = new javax.swing.GroupLayout(panelOperatorsMutation);
        panelOperatorsMutation.setLayout(panelOperatorsMutationLayout);
        panelOperatorsMutationLayout.setHorizontalGroup(
            panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsMutationLayout.createSequentialGroup()
                .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkFeatureMutation)
                    .addGroup(panelOperatorsMutationLayout.createSequentialGroup()
                        .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkMoveMethod)
                            .addComponent(checkAddClass))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkMoveOperation)
                            .addComponent(checkManagerClass)
                            .addComponent(checkMoveAttribute))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOperatorsMutationLayout.setVerticalGroup(
            panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsMutationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkFeatureMutation)
                    .addComponent(checkMoveOperation))
                .addGap(5, 5, 5)
                .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkMoveMethod)
                    .addComponent(checkManagerClass))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOperatorsMutationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkAddClass)
                    .addComponent(checkMoveAttribute))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setText("Population Size:");

        fieldPopulationSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldPopulationSizeFocusLost(evt);
            }
        });
        fieldPopulationSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldPopulationSizeKeyTyped(evt);
            }
        });

        labelArchivePAES.setText("Archive Size:");

        javax.swing.GroupLayout panelExperimentSettingsLayout = new javax.swing.GroupLayout(panelExperimentSettings);
        panelExperimentSettings.setLayout(panelExperimentSettingsLayout);
        panelExperimentSettingsLayout.setHorizontalGroup(
            panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                        .addComponent(panelCrossProb, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelMutationProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelOperators)
                            .addComponent(comboAlgorithms, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(fieldNumberOfRuns))
                                .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                    .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelExperimentSettingsLayout.createSequentialGroup()
                                            .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel4)
                                                .addComponent(jLabel5))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                        .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                            .addComponent(labelArchivePAES)
                                            .addGap(32, 32, 32)))
                                    .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(fieldPaesArchiveSize, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(fieldMaxEvaluations, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                                            .addComponent(fieldPopulationSize)))))
                            .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(checkMutation)
                                .addGap(18, 18, 18)
                                .addComponent(checkCrossover))
                            .addComponent(labelAlgorithms))
                        .addGap(136, 136, 136)
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(panelMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelOperatorsMutation, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelExperimentSettingsLayout.setVerticalGroup(
            panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelAlgorithms)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboAlgorithms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(fieldNumberOfRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(fieldMaxEvaluations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(labelArchivePAES)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(fieldPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(fieldPaesArchiveSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelOperators, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkCrossover, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkMutation)))
                    .addGroup(panelExperimentSettingsLayout.createSequentialGroup()
                        .addComponent(panelMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelOperatorsMutation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(panelExperimentSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelCrossProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMutationProb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(113, 113, 113))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input Architecture(s)", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        jLabel11.setText("A list of paths separated by comma");

        fieldArchitectureInput.setColumns(20);
        fieldArchitectureInput.setRows(5);
        jScrollPane2.setViewportView(fieldArchitectureInput);

        btnCleanListArchs1.setText("Clean");
        btnCleanListArchs1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanListArchs1ActionPerformed(evt);
            }
        });

        btnInput1.setText("Confirme");
        btnInput1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInput1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(btnInput1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCleanListArchs1))
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel11)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCleanListArchs1)
                    .addComponent(btnInput1))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select where you want to save outputs", 0, 0, new java.awt.Font("Verdana", 1, 14), java.awt.Color.magenta)); // NOI18N

        fieldOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldOutputActionPerformed(evt);
            }
        });

        btnOutput.setText("Select a Directory...");
        btnOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnOutput)
                    .addComponent(fieldOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fieldOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnOutput)
                .addContainerGap())
        );

        btnRun.setBackground(new java.awt.Color(255, 204, 102));
        btnRun.setForeground(new java.awt.Color(0, 153, 0));
        btnRun.setText("RUN");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        jLabel19.setText("Set a description for this execution: (Optional)");

        executionDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                executionDescriptionKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout algorithmsLayout = new javax.swing.GroupLayout(algorithms);
        algorithms.setLayout(algorithmsLayout);
        algorithmsLayout.setHorizontalGroup(
            algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelExperimentSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(algorithmsLayout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                        .addGroup(algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(executionDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algorithmsLayout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, algorithmsLayout.createSequentialGroup()
                                .addComponent(btnRun)
                                .addGap(21, 21, 21))))))
        );
        algorithmsLayout.setVerticalGroup(
            algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelExperimentSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(algorithmsLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(algorithmsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(algorithmsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(executionDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(algorithmsLayout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(btnRun, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Execution Configurations", algorithms);

        jPanel4.setName("DesignPatterns");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Design Pattern Selection Strategy", 0, 0, new java.awt.Font("Verdana", 1, 14), new java.awt.Color(255, 0, 255))); // NOI18N

        checkMediator.setText("Mediator");
        checkMediator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMediatorActionPerformed(evt);
            }
        });

        checkStrategy.setText("Strategy");
        checkStrategy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkStrategyActionPerformed(evt);
            }
        });

        checkBridge.setText("Bridge");
        checkBridge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBridgeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBridge)
                    .addComponent(checkStrategy)
                    .addComponent(checkMediator))
                .addContainerGap(226, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(checkMediator)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkStrategy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBridge)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelPatternScope.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scope Selection Strategy", 0, 0, new java.awt.Font("Verdana", 1, 14), new java.awt.Color(255, 0, 255))); // NOI18N

        buttonGroup1.add(radioRandomStrategy);
        radioRandomStrategy.setSelected(true);
        radioRandomStrategy.setText("Random");
        radioRandomStrategy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioRandomStrategyActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioElementsWithSameDPorNone);
        radioElementsWithSameDPorNone.setText("Elements With Same Design Pattern or None");
        radioElementsWithSameDPorNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioElementsWithSameDPorNoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPatternScopeLayout = new javax.swing.GroupLayout(panelPatternScope);
        panelPatternScope.setLayout(panelPatternScopeLayout);
        panelPatternScopeLayout.setHorizontalGroup(
            panelPatternScopeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPatternScopeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPatternScopeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioRandomStrategy)
                    .addComponent(radioElementsWithSameDPorNone))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelPatternScopeLayout.setVerticalGroup(
            panelPatternScopeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPatternScopeLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(radioRandomStrategy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioElementsWithSameDPorNone)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelPatternScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(258, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelPatternScope, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(481, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Design Patterns", jPanel4);

        experiments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                experimentsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                experimentsMouseEntered(evt);
            }
        });
        experiments.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                experimentsFocusGained(evt);
            }
        });

        tableExp.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "name", "algorithm", "Created at"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableExp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableExpMouseClicked(evt);
            }
        });
        tableExp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableExpKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tableExp);

        jLabel13.setFont(new java.awt.Font("Monaco", 1, 18)); // NOI18N
        jLabel13.setText("Executions");

        tableExecutions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableExecutions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableExecutionsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tableExecutionsMouseEntered(evt);
            }
        });
        jScrollPane3.setViewportView(tableExecutions);

        jLabel16.setFont(new java.awt.Font("Monaco", 1, 18)); // NOI18N
        jLabel16.setText("Runs");

        javax.swing.GroupLayout panelExecutionsLayout = new javax.swing.GroupLayout(panelExecutions);
        panelExecutions.setLayout(panelExecutionsLayout);
        panelExecutionsLayout.setHorizontalGroup(
            panelExecutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelExecutionsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelExecutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        panelExecutionsLayout.setVerticalGroup(
            panelExecutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelExecutionsLayout.createSequentialGroup()
                .addComponent(jLabel16)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel17.setText("Solution:");

        comboSolutions.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboSolutionsItemStateChanged(evt);
            }
        });
        comboSolutions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSolutionsActionPerformed(evt);
            }
        });
        comboSolutions.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                comboSolutionsFocusLost(evt);
            }
        });

        javax.swing.GroupLayout panelSolutionsLayout = new javax.swing.GroupLayout(panelSolutions);
        panelSolutions.setLayout(panelSolutionsLayout);
        panelSolutionsLayout.setHorizontalGroup(
            panelSolutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSolutionsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                .addComponent(comboSolutions, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelSolutionsLayout.setVerticalGroup(
            panelSolutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSolutionsLayout.createSequentialGroup()
                .addGroup(panelSolutionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(comboSolutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 10, Short.MAX_VALUE))
        );

        tableObjectives.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tableObjectives);

        comboMetrics.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboMetricsItemStateChanged(evt);
            }
        });
        comboMetrics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMetricsActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel9.setText("Objective Functions");

        javax.swing.GroupLayout panelObjectivesLayout = new javax.swing.GroupLayout(panelObjectives);
        panelObjectives.setLayout(panelObjectivesLayout);
        panelObjectivesLayout.setHorizontalGroup(
            panelObjectivesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelObjectivesLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(comboMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelObjectivesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelObjectivesLayout.setVerticalGroup(
            panelObjectivesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelObjectivesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelObjectivesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableMetrics.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(tableMetrics);

        bestSolutions.setText("Non-Dominated Solutions");
        bestSolutions.setEnabled(false);
        bestSolutions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bestSolutionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelShowMetricsLayout = new javax.swing.GroupLayout(panelShowMetrics);
        panelShowMetrics.setLayout(panelShowMetricsLayout);
        panelShowMetricsLayout.setHorizontalGroup(
            panelShowMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShowMetricsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
            .addGroup(panelShowMetricsLayout.createSequentialGroup()
                .addComponent(bestSolutions)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelShowMetricsLayout.setVerticalGroup(
            panelShowMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShowMetricsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(bestSolutions)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnShowConfigurations.setText("Show Configurations");
        btnShowConfigurations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowConfigurationsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout experimentsLayout = new javax.swing.GroupLayout(experiments);
        experiments.setLayout(experimentsLayout);
        experimentsLayout.setHorizontalGroup(
            experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(experimentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(experimentsLayout.createSequentialGroup()
                        .addGroup(experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(experimentsLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnShowConfigurations)))
                        .addGap(18, 18, 18)
                        .addComponent(panelExecutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelObjectives, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelShowMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelSolutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        experimentsLayout.setVerticalGroup(
            experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(experimentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(experimentsLayout.createSequentialGroup()
                        .addGroup(experimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnShowConfigurations)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelExecutions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(panelSolutions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(1, 1, 1)
                .addComponent(panelObjectives, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(panelShowMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        jTabbedPane1.addTab("Results", experiments);

        tableExp2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "id", "name", "algorithm", "Created at"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableExp2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableExp2MouseClicked(evt);
            }
        });
        tableExp2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableExp2KeyPressed(evt);
            }
        });
        jScrollPane7.setViewportView(tableExp2);

        panelFunctionExecutionsSelecteds.setBorder(javax.swing.BorderFactory.createTitledBorder("Solutions in the Search Space"));
        panelFunctionExecutionsSelecteds.setMinimumSize(new java.awt.Dimension(300, 100));

        jButton2.setText("Select Objective Functions");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btnGenerateChart.setText("Generate Chart");
        btnGenerateChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateChartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelFunctionExecutionsSelectedsLayout = new javax.swing.GroupLayout(panelFunctionExecutionsSelecteds);
        panelFunctionExecutionsSelecteds.setLayout(panelFunctionExecutionsSelectedsLayout);
        panelFunctionExecutionsSelectedsLayout.setHorizontalGroup(
            panelFunctionExecutionsSelectedsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFunctionExecutionsSelectedsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerateChart)
                .addContainerGap(115, Short.MAX_VALUE))
        );
        panelFunctionExecutionsSelectedsLayout.setVerticalGroup(
            panelFunctionExecutionsSelectedsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFunctionExecutionsSelectedsLayout.createSequentialGroup()
                .addGroup(panelFunctionExecutionsSelectedsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(btnGenerateChart))
                .addGap(0, 170, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Hypervolume"));

        btnHypervolume.setText("Hypervolume");
        btnHypervolume.setEnabled(!OsUtils.isWindows());
        btnHypervolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHypervolumeActionPerformed(evt);
            }
        });

        hypervolumeNormalized.setText("Use Normalization ?");
        hypervolumeNormalized.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hypervolumeNormalizedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnHypervolume)
                .addGap(18, 18, 18)
                .addComponent(hypervolumeNormalized)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnHypervolume)
                .addComponent(hypervolumeNormalized))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Euclidean Distance"));

        btnGenerateEdChart.setText("Number of Solutions per Euclidean Distance");
        btnGenerateEdChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateEdChartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(btnGenerateEdChart)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnGenerateEdChart)
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelFunctionExecutionsSelecteds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 11, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(301, Short.MAX_VALUE))
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(panelFunctionExecutionsSelecteds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Experiments", jPanel9);

        textLogsArea.setColumns(20);
        textLogsArea.setRows(5);
        jScrollPane6.setViewportView(textLogsArea);

        jLabel12.setText("Status: -");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(199, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Logs", jPanel6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 990, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Paths Confs");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConcernProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConcernProfileActionPerformed
      String newPath = fileChooser(fieldConcernProfile, "uml");

      if (newPath.equals("")) {
        this.config.updatePathToProfileConcerns(fieldConcernProfile.getText());
      } else {
        this.config.updatePathToProfileConcerns(newPath);
      }
    }//GEN-LAST:event_btnConcernProfileActionPerformed

    private void btnSmartyProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSmartyProfileActionPerformed
      String newPath = fileChooser(fieldSmartyProfile, "uml");

      if (newPath.equals("")) {
        this.config.updatePathToProfileSmarty(fieldSmartyProfile.getText());
      } else {
        this.config.updatePathToProfileSmarty(newPath);
      }
    }//GEN-LAST:event_btnSmartyProfileActionPerformed

    private void fieldConcernProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldConcernProfileActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_fieldConcernProfileActionPerformed

    private void fieldSmartyProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldSmartyProfileActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_fieldSmartyProfileActionPerformed

    private void btnTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTemplateActionPerformed
      String path = dirChooser(fieldTemplate);

      if ("".equals(path)) {
        this.config.updatePathToTemplateFiles(fieldTemplate.getText() + UserHome.getFileSeparator());
      } else {
        this.config.updatePathToTemplateFiles(path + UserHome.getFileSeparator());
      }
    }//GEN-LAST:event_btnTemplateActionPerformed

  private String dirChooser(JTextField field) throws HeadlessException {
    JFileChooser c = new JFileChooser();
    String path;
    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int rVal = c.showOpenDialog(this);

    if (rVal == JFileChooser.APPROVE_OPTION) {
      path = c.getSelectedFile().getAbsolutePath();
      field.setText(path + UserHome.getFileSeparator());
      field.updateUI();
      return path;
    }

    return "";
  }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      StringBuffer sb = new StringBuffer();
      sb.append("Application.yml file content").append("\n\n");
      sb.append("directoryToExportModels: ").append(config.getConfig().getDirectoryToExportModels()).append("\n");
      sb.append("pathToProfile: ").append(config.getConfig().getPathToProfile()).append("\n");
      sb.append("pathToProfileConcern: ").append(config.getConfig().getPathToProfileConcern()).append("\n");
      sb.append("pathToProfilePatterns: ").append(config.getConfig().getPathToProfilePatterns()).append("\n");
      sb.append("pathToProfileRelationships: ").append(config.getConfig().getPathToProfileRelationships()).append("\n");
      sb.append("pathToTemplateModelsDirectory: ").append(config.getConfig().getPathToTemplateModelsDirectory()).append("\n");
      JOptionPane.showMessageDialog(null, sb);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnManipulationDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManipulationDirActionPerformed
      String path = dirChooser(fieldManipulationDir);
      if ("".equals(path)) {
        this.config.updatePathToSaveModels(fieldManipulationDir.getText() + UserHome.getFileSeparator());
      } else {
        this.config.updatePathToSaveModels(path + UserHome.getFileSeparator());
      }
    }//GEN-LAST:event_btnManipulationDirActionPerformed

    private void checkSmartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkSmartyActionPerformed
      if (!checkSmarty.isSelected()) {
        pathSmartyBck = fieldSmartyProfile.getText();
        fieldSmartyProfile.setText("");
        btnSmartyProfile.setEnabled(false);
        this.config.updatePathToProfileSmarty("");
      } else {
        btnSmartyProfile.setEnabled(true);
        fieldSmartyProfile.setText(pathSmartyBck);
        this.config.updatePathToProfileSmarty(pathSmartyBck);
      }
    }//GEN-LAST:event_checkSmartyActionPerformed

    private void checkConcernsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkConcernsActionPerformed
      if (!checkConcerns.isSelected()) {
        pathConcernBck = fieldConcernProfile.getText();
        fieldConcernProfile.setText("");
        btnConcernProfile.setEnabled(false);
        this.config.updatePathToProfileConcerns("");
      } else {
        btnConcernProfile.setEnabled(true);
        fieldConcernProfile.setText(pathConcernBck);
        this.config.updatePathToProfileConcerns(pathConcernBck);
      }
    }//GEN-LAST:event_checkConcernsActionPerformed

    private void comboAlgorithmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboAlgorithmsActionPerformed
      if (comboAlgorithms.getSelectedItem() != null && comboAlgorithms.getSelectedIndex() != 0) {
        String algorithmName = comboAlgorithms.getSelectedItem().toString();
        VolatileConfs.setAlgorithmName(algorithmName);
        if ("Paes".equalsIgnoreCase(algorithmName)) {
          enableFieldForPaes();
          hideFieldsForNSGAII();
        }
        if ("NSGA-II".equalsIgnoreCase(algorithmName)) {
          enableFieldsForNSGAII();
          hideFieldsForPases();
        }
        Logger.getLogger().putLog(String.format("Selected: %s", comboAlgorithms.getSelectedItem().toString()),
                Level.INFO, main.class.getName());
      } else {
        VolatileConfs.setAlgorithmName(null);
      }
    }//GEN-LAST:event_comboAlgorithmsActionPerformed

    private void checkMutationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMutationActionPerformed
      if (!checkMutation.isSelected()) {
        panelOperatorsMutation.setVisible(false);
        panelMutationProb.setVisible(false);
      } else {
        panelOperatorsMutation.setVisible(true);
        if (crossoverProbabilityBck != null) {
          fieldCrossoverProbability.setText(crossoverProbabilityBck);
        }
        panelMutationProb.setVisible(true);
      }
    }//GEN-LAST:event_checkMutationActionPerformed

    private void checkAddClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAddClassActionPerformed
      addOrRemoveOperatorMutation(FeatureMutationOperators.ADD_CLASS_MUTATION.getOperatorName(), checkAddClass);
    }//GEN-LAST:event_checkAddClassActionPerformed

    private void checkMoveAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMoveAttributeActionPerformed
      addOrRemoveOperatorMutation(FeatureMutationOperators.MOVE_ATTRIBUTE_MUTATION.getOperatorName(), checkMoveAttribute);
    }//GEN-LAST:event_checkMoveAttributeActionPerformed

    private void checkFeatureMutationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFeatureMutationActionPerformed
      addOrRemoveOperatorMutation(FeatureMutationOperators.FEATURE_MUTATION.getOperatorName(), checkFeatureMutation);
    }//GEN-LAST:event_checkFeatureMutationActionPerformed

    private void checkMoveMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMoveMethodActionPerformed
      addOrRemoveOperatorMutation(FeatureMutationOperators.MOVE_METHOD_MUTATION.getOperatorName(), checkMoveMethod);
    }//GEN-LAST:event_checkMoveMethodActionPerformed

    private void checkMoveOperationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMoveOperationActionPerformed
      addOrRemoveOperatorMutation(FeatureMutationOperators.MOVE_OPERATION_MUTATION.getOperatorName(), checkMoveOperation);
    }//GEN-LAST:event_checkMoveOperationActionPerformed

    private void crossProbSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_crossProbSliderMouseDragged
      double a = (double) crossProbSlider.getValue() / 10;
      fieldCrossoverProbability.setText(String.valueOf(a));
    }//GEN-LAST:event_crossProbSliderMouseDragged

    private void checkCrossoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCrossoverActionPerformed
      if (checkCrossover.isSelected()) {
        if (crossoverProbabilityBck != null) {
          fieldCrossoverProbability.setText(crossoverProbabilityBck);
        }
        panelCrossProb.setVisible(true);
      } else {
        crossoverProbabilityBck = fieldCrossoverProbability.getText();
        fieldCrossoverProbability.setText("0");
        panelCrossProb.setVisible(false);
      }
    }//GEN-LAST:event_checkCrossoverActionPerformed

    private void mutatinProbSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mutatinProbSliderMouseDragged
      double a = (double) mutatinProbSlider.getValue() / 10;
      fieldMutationProb.setText(String.valueOf(a));
    }//GEN-LAST:event_mutatinProbSliderMouseDragged

    private void btnCleanListArchs1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanListArchs1ActionPerformed
      fieldArchitectureInput.setText("");
      VolatileConfs.setArchitectureInputPath(null);
    }//GEN-LAST:event_btnCleanListArchs1ActionPerformed

    private void btnInput1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInput1ActionPerformed
      Validators.validateEntries(fieldArchitectureInput.getText());
    }//GEN-LAST:event_btnInput1ActionPerformed

    private void fieldOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldOutputActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_fieldOutputActionPerformed

    private void btnOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOutputActionPerformed
      String path = dirChooser(fieldOutput);
      if ("".equals(path)) {
        this.config.updatePathToExportModels(fieldOutput.getText() + UserHome.getFileSeparator());
      } else {
        this.config.updatePathToExportModels(path + UserHome.getFileSeparator());
      }
    }//GEN-LAST:event_btnOutputActionPerformed

    private void comboAlgorithmsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboAlgorithmsItemStateChanged
    }//GEN-LAST:event_comboAlgorithmsItemStateChanged

    private void checkEleganceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEleganceActionPerformed
      final String metric = Metrics.ELEGANCE.getName();
      addToMetrics(checkElegance, metric);
    }//GEN-LAST:event_checkEleganceActionPerformed

    private void checkConventionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkConventionalActionPerformed
      final String metric = Metrics.CONVENTIONAL.getName();
      addToMetrics(checkConventional, metric);
    }//GEN-LAST:event_checkConventionalActionPerformed

    private void checkPLAExtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkPLAExtActionPerformed
      final String metric = Metrics.PLA_EXTENSIBILIY.getName();
      addToMetrics(checkPLAExt, metric);
    }//GEN-LAST:event_checkPLAExtActionPerformed

    private void checkFeatureDrivenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFeatureDrivenActionPerformed
      final String metric = Metrics.FEATURE_DRIVEN.getName();
      addToMetrics(checkFeatureDriven, metric);
    }//GEN-LAST:event_checkFeatureDrivenActionPerformed

    private void numberOfRunsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numberOfRunsFocusLost
      if (Utils.isDigit(fieldNumberOfRuns.getText())) {
        Logger.getLogger().putLog(String.format(String.format("Number Of Runs: %s", fieldNumberOfRuns.getText()),
                Level.INFO, main.class.getName()));
        VolatileConfs.setNumberOfRuns(Integer.parseInt(fieldNumberOfRuns.getText()));
      }
    }//GEN-LAST:event_numberOfRunsFocusLost

    private void fieldNumberOfRunsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldNumberOfRunsActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_fieldNumberOfRunsActionPerformed

    private void fieldMaxEvaluationsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldMaxEvaluationsFocusLost
      if (Utils.isDigit(fieldMaxEvaluations.getText())) {
        Logger.getLogger().putLog(String.format(String.format("Max Evaluations: %s", fieldMaxEvaluations.getText()),
                Level.INFO, main.class.getName()));
        VolatileConfs.setMaxEvaluations(Integer.parseInt(fieldMaxEvaluations.getText()));
      }
    }//GEN-LAST:event_fieldMaxEvaluationsFocusLost

    private void fieldPopulationSizeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldPopulationSizeFocusLost
      if (Utils.isDigit(fieldPopulationSize.getText())) {
        Logger.getLogger().putLog(String.format(String.format("Population Size: %s", fieldPopulationSize.getText()),
                Level.INFO, main.class.getName()));
        VolatileConfs.setPopulationSize(Integer.parseInt(fieldPopulationSize.getText()));
      }
    }//GEN-LAST:event_fieldPopulationSizeFocusLost

    private void fieldNumberOfRunsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumberOfRunsKeyTyped
    onlyDigit(evt);
  }

  private boolean onlyDigit(KeyEvent evt) {
    char c = evt.getKeyChar();
    if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || c == KeyEvent.VK_DELETE)) {
      getToolkit().beep();
      evt.consume();
      return false;
    }
    return true;
    }//GEN-LAST:event_fieldNumberOfRunsKeyTyped

    private void fieldMaxEvaluationsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldMaxEvaluationsKeyTyped
      onlyDigit(evt);
    }//GEN-LAST:event_fieldMaxEvaluationsKeyTyped

    private void fieldPopulationSizeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldPopulationSizeKeyTyped
      onlyDigit(evt);
    }//GEN-LAST:event_fieldPopulationSizeKeyTyped

  /**
   * Rodar experimento
   *
   * @param evt
   */
    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed

      //Validacoes inicias
      //Verifica se as entradas sao validas. Caso contrario finaliza
      if (Validators.validateEntries(fieldArchitectureInput.getText())) {
        return;
      }

      //Recupera o algoritmo selecionado pelo usuário
      String algoritmToRun = VolatileConfs.getAlgorithmName();

      //Caso nenhum for selecionado, informa o usuario
      if (comboAlgorithms.getSelectedIndex() == 0) {
        JOptionPane.showMessageDialog(this, "You need select a algorithm");
      } else {
        //Pede confirmacao para o usuario para de fato executar o
        //experimento.
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult = JOptionPane.showConfirmDialog(this, "This will take a time."
                + " Meanwhile the UI will be blocked",
                "You have sure?", dialogButton);
        //Caso usuário aceite, verifica qual algoritmo executar
        //E invoca a classe responsável.
        if (dialogResult == 0) {
          if ("NSGA-II".equalsIgnoreCase(algoritmToRun)) {
            SwingWorker sw = new SwingWorker() {

              @Override
              protected Object doInBackground() throws Exception {
                jLabel12.setText("Working... wait. Started " + Time.timeNow());
                Logger.getLogger().putLog("Execution NSGAII...");
                jTabbedPane1.setSelectedIndex(5);
                btnRun.setEnabled(false);
                blockUITabs();
                executeNSGAII();
                return 0;
              }

              @Override
              protected void done() {
                jLabel12.setText("Done");
                progressBar.setIndeterminate(false);
                Logger.getLogger().putLog(String.format("Done NSGAII Execution at: %s", Time.timeNow().toString()));
                db.Database.reloadContent();
              }
            };

            sw.execute();
            progressBar.setIndeterminate(true);

          }
          if ("PAES".equalsIgnoreCase(algoritmToRun)) {
            SwingWorker sw2 = new SwingWorker() {

              @Override
              protected Object doInBackground() throws Exception {
                jLabel12.setText("Working... wait. Started " + Time.timeNow());
                Logger.getLogger().putLog("Execution PAES...");
                jTabbedPane1.setSelectedIndex(5);
                btnRun.setEnabled(false);
                blockUITabs();
                executePAES();
                return 0;
              }

              @Override
              protected void done() {
                jLabel12.setText("Done");
                progressBar.setIndeterminate(false);
                Logger.getLogger().putLog(String.format("Done PAES Execution at: %s", Time.timeNow().toString()));
                db.Database.reloadContent();
              }
            };

            sw2.execute();
            progressBar.setIndeterminate(true);
          }

        }
      }
    }//GEN-LAST:event_btnRunActionPerformed

    private void tableExpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableExpMouseClicked
      if (evt.getClickCount() == 2) {
        panelShowMetrics.setVisible(false);
        JTable target = (JTable) evt.getSource();
        int rowIndex = target.getSelectedRow();
        String idExperiment = target.getModel().getValueAt(rowIndex, 0).toString();

        GuiUtils.hideSolutionsAndExecutionPaneIfExperimentSelectedChange(
                this.selectedExperiment, idExperiment, panelSolutions,
                panelObjectives);

        this.selectedExperiment = idExperiment;
        createTableExecutions(idExperiment);
        bestSolutions.setEnabled(true);
        btnShowConfigurations.setEnabled(true);
      }
    }//GEN-LAST:event_tableExpMouseClicked

    private void tableExpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableExpKeyPressed
      // TODO add your handling code here:
    }//GEN-LAST:event_tableExpKeyPressed

    private void tableExecutionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableExecutionsMouseClicked
      if (evt.getClickCount() == 2) {
        panelSolutions.setVisible(true);

        JTable target = (JTable) evt.getSource();
        int rowIndex = target.getSelectedRow();
        String idExecution = target.getModel().getValueAt(rowIndex, 0).toString();
        this.selectedExecution = idExecution;

        List<String> solutions = db.Database.getAllSolutionsForExecution(selectedExperiment, idExecution);

        comboSolutions.setModel(new SolutionsComboBoxModel(idExecution, solutions));
        comboSolutions.setSelectedIndex(0);
      }
    }//GEN-LAST:event_tableExecutionsMouseClicked

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void experimentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_experimentsMouseClicked
    }//GEN-LAST:event_experimentsMouseClicked

    private void comboSolutionsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboSolutionsItemStateChanged
    }//GEN-LAST:event_comboSolutionsItemStateChanged

    private void comboSolutionsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comboSolutionsFocusLost
      // TODO add your handling code here:
    }//GEN-LAST:event_comboSolutionsFocusLost

  private void comboSolutionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSolutionsActionPerformed
    panelShowMetrics.setVisible(false);
    GuiServices.initializerComboObjectiveFunctions(comboMetrics, this.selectedExperiment);

    Map<String, String> objectives = db.Database.getAllObjectivesByExecution(((Solution) comboSolutions.getSelectedItem()).getId(), this.selectedExperiment);

    String fileName = ((Solution) comboSolutions.getSelectedItem()).getName();
    String objectiveId = Utils.extractSolutionIdFromSolutionFileName(fileName);
    Map<String, String> r = GuiUtils.formatObjectives(objectives.get(objectiveId), this.selectedExperiment);

    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Objective Function");
    model.addColumn("Value");

    GuiUtils.makeTableNotEditable(tableObjectives);
    tableObjectives.setModel(model);

    Iterator<Entry<String, String>> it = r.entrySet().iterator();
    while (it.hasNext()) {
      Object[] row = new Object[2];
      Map.Entry pairs = (Map.Entry<String, String>) it.next();
      row[0] = pairs.getKey();
      row[1] = pairs.getValue();
      it.remove(); // evitar ConcurrentModificationException
      model.addRow(row);
    }

    panelObjectives.setVisible(true);
  }//GEN-LAST:event_comboSolutionsActionPerformed

  private void comboMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMetricsActionPerformed
  }//GEN-LAST:event_comboMetricsActionPerformed

  private void comboMetricsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboMetricsItemStateChanged
    String selectedMetric = comboMetrics.getSelectedItem().toString().toLowerCase().replaceAll("\\s+", "");

    Map<String, String[]> mapColumns = new HashMap<>();
    String[] plaExtColumns = {"PLA Extensibility"};
    String[] eleganceColumns = {"NAC", "ATMR", "EC"};
    String[] conventionalsColumns = {"macAggregation", "choesion", "meanDepComps",
      "meanNumOps", "sumClassesDepIn", "sumClassesDepOut", "sumDepIn", "sumDepOut"};
    String[] featureColumns = {"msiAggregation", "cdac ", "cdai", "cdao",
      "cibc", "iibc", "oobc", "lcc", "lccClass", "cdaClass", "cibClass"};

    mapColumns.put("plaextensibility", plaExtColumns);
    mapColumns.put("elegance", eleganceColumns);
    mapColumns.put("conventional", conventionalsColumns);
    mapColumns.put("feature", featureColumns);

    DefaultTableModel model = new DefaultTableModel();
    tableMetrics.setModel(model);
    int numberOfColumns = 0;

    if (comboSolutions.getSelectedItem() != null) {
      String idSolution = Utils.extractSolutionIdFromSolutionFileName(comboSolutions.getSelectedItem().toString());

      if (selectedMetric.equalsIgnoreCase("plaextensibility")) {
        PLAExtensibility plaExt = db.Database.getPlaExtMetricsForSolution(idSolution, this.selectedExperiment);

        for (int i = 0; i < mapColumns.get(selectedMetric).length; i++) {
          model.addColumn(mapColumns.get("plaextensibility")[i]);
          numberOfColumns++;
        }

        Object[] row = new Object[numberOfColumns + 2];
        row[0] = plaExt.getPlaExtensibility();
        row[1] = this.selectedExperiment;
        row[2] = this.selectedExecution;
        model.addRow(row);
      } else if (selectedMetric.equalsIgnoreCase("elegance")) {
        Elegance elegance = db.Database.getEleganceMetricsForSolution(idSolution, this.selectedExperiment);

        for (int i = 0; i < mapColumns.get(selectedMetric).length; i++) {
          model.addColumn(mapColumns.get("elegance")[i]);
          numberOfColumns++;
        }

        Object[] row = new Object[numberOfColumns + 2];
        row[0] = elegance.getNac();
        row[1] = elegance.getAtmr();
        row[2] = elegance.getEc();
        model.addRow(row);
      } else if (selectedMetric.equalsIgnoreCase("conventional")) {
        Conventional conventional = db.Database.getConventionalsMetricsForSolution(idSolution, this.selectedExperiment);

        for (int i = 0; i < mapColumns.get(selectedMetric).length; i++) {
          model.addColumn(mapColumns.get("conventional")[i]);
          numberOfColumns++;
        }

        Object[] row = new Object[numberOfColumns + 2];
        row[0] = conventional.getMacAggregation();
        row[1] = conventional.getCohesion();
        row[2] = conventional.getMeanDepComps();
        row[3] = conventional.getMeanNumOps();
        row[4] = conventional.getSumClassesDepIn();
        row[5] = conventional.getSumClassesDepOut();
        row[6] = conventional.getSumDepIn();
        row[7] = conventional.getSumDepOut();
        model.addRow(row);
      } else if (selectedMetric.equalsIgnoreCase("featuredriven")) {
        FeatureDriven f = db.Database.getFeatureDrivenMetricsForSolution(idSolution, this.selectedExperiment);

        for (int i = 0; i < mapColumns.get("feature").length; i++) {
          model.addColumn(mapColumns.get("feature")[i]);
          numberOfColumns++;
        }

        Object[] row = new Object[numberOfColumns + 2];
        row[0] = f.getMsiAggregation();
        row[1] = f.getCdac();
        row[2] = f.getCdai();
        row[3] = f.getCdao();
        row[4] = f.getCibc();
        row[5] = f.getIibc();
        row[6] = f.getOobc();
        row[7] = f.getLcc();
        row[8] = f.getLccClass();
        row[9] = f.getCdaClass();
        row[10] = f.getCibClass();
        model.addRow(row);

      }
      panelShowMetrics.setVisible(true);
    }
  }//GEN-LAST:event_comboMetricsItemStateChanged

  private void tableExecutionsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableExecutionsMouseEntered
    // TODO add your handling code here:
  }//GEN-LAST:event_tableExecutionsMouseEntered

  private void experimentsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_experimentsFocusGained
  }//GEN-LAST:event_experimentsFocusGained

  private void experimentsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_experimentsMouseEntered
    // TODO add your handling code here:
  }//GEN-LAST:event_experimentsMouseEntered

  private void bestSolutionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bestSolutionsActionPerformed
    SmallerFintnessValuesWindow sfvw = new SmallerFintnessValuesWindow();

    sfvw.setVisible(true);
    sfvw.setTitle(titleWindow());
    sfvw.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    sfvw.setExperimentId(selectedExperiment);
    sfvw.enablePanelsObjectiveFunctions();
    sfvw.loadEds();
  }//GEN-LAST:event_bestSolutionsActionPerformed

  private void btnPatternProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPatternProfileActionPerformed
    String newPath = fileChooser(fieldPatternsProfile, "uml");
    if (newPath.equals("")) {
      this.config.updatePathToProfilePatterns(fieldPatternsProfile.getText());
    } else {
      this.config.updatePathToProfilePatterns(newPath);
    }
  }//GEN-LAST:event_btnPatternProfileActionPerformed

  private void checkPatternsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkPatternsActionPerformed
    if (!checkPatterns.isSelected()) {
      pathPatternsBck = fieldPatternsProfile.getText();
      fieldPatternsProfile.setText("");
      btnPatternProfile.setEnabled(false);
      this.config.updatePathToProfilePatterns("");
    } else {
      btnPatternProfile.setEnabled(true);
      fieldPatternsProfile.setText(pathPatternsBck);
      this.config.updatePathToProfilePatterns(pathPatternsBck);
    }
  }//GEN-LAST:event_checkPatternsActionPerformed

  private void btnRelationshipProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRelationshipProfileActionPerformed
    String newPath = fileChooser(fieldRelationshipsProfile, "uml");
    if (newPath.equals("")) {
      this.config.updatePathToProfileRelationships(fieldRelationshipsProfile.getText());
    } else {
      this.config.updatePathToProfileRelationships(newPath);
    }
  }//GEN-LAST:event_btnRelationshipProfileActionPerformed

  private void checkRelationshipsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRelationshipsActionPerformed
    if (!checkRelationships.isSelected()) {
      pathRelationshipsBck = fieldRelationshipsProfile.getText();
      fieldRelationshipsProfile.setText("");
      btnRelationshipProfile.setEnabled(false);
      this.config.updatePathToProfileRelationships("");
    } else {
      btnRelationshipProfile.setEnabled(true);
      fieldRelationshipsProfile.setText(pathRelationshipsBck);
      this.config.updatePathToProfileRelationships(pathRelationshipsBck);
    }
  }//GEN-LAST:event_checkRelationshipsActionPerformed

  private void checkBridgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBridgeActionPerformed
    addFlagOperatorDesignPatternUsed();
    addOrRemovePatternToApply("Bridge", checkBridge);
  }//GEN-LAST:event_checkBridgeActionPerformed

  private void checkMediatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMediatorActionPerformed
    addFlagOperatorDesignPatternUsed();
    addOrRemovePatternToApply("Strategy", checkMediator);
  }//GEN-LAST:event_checkMediatorActionPerformed

  private void checkManagerClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkManagerClassActionPerformed
    addOrRemoveOperatorMutation(FeatureMutationOperators.ADD_MANAGER_CLASS_MUTATION.getOperatorName(), checkManagerClass);
  }//GEN-LAST:event_checkManagerClassActionPerformed

  private void checkStrategyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkStrategyActionPerformed
    addFlagOperatorDesignPatternUsed();
    addOrRemovePatternToApply("Mediator", checkStrategy);
  }//GEN-LAST:event_checkStrategyActionPerformed

  private void radioElementsWithSameDPorNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioElementsWithSameDPorNoneActionPerformed
    if (this.ewsdp == null) {
      this.ewsdp = new ElementsWithSameDesignPatternSelection();
    }
    VolatileConfs.setScopePatterns(ewsdp);
  }//GEN-LAST:event_radioElementsWithSameDPorNoneActionPerformed

  private void radioRandomStrategyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioRandomStrategyActionPerformed
    VolatileConfs.setScopePatterns(null);
  }//GEN-LAST:event_radioRandomStrategyActionPerformed

  private void btnShowConfigurationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowConfigurationsActionPerformed
    ShowConfigurations showConfs = new ShowConfigurations();

    showConfs.setVisible(true);
    showConfs.setTitle(titleWindow());
    showConfs.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    showConfs.setExperimentId(selectedExperiment);
    showConfs.setDirOutput(this.config.getConfig().getDirectoryToExportModels());
    showConfs.fillFields();

  }//GEN-LAST:event_btnShowConfigurationsActionPerformed

  private void tableExp2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableExp2MouseClicked
    // TODO add your handling code here:
  }//GEN-LAST:event_tableExp2MouseClicked

  private void tableExp2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableExp2KeyPressed
    // TODO add your handling code here:
  }//GEN-LAST:event_tableExp2KeyPressed

  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    panelFunctionExecutionsSelecteds.setLayout(new MigLayout());

    for (Component comp : panelFunctionExecutionsSelecteds.getComponents()) {
      if (comp instanceof JCheckBox) {
        panelFunctionExecutionsSelecteds.remove((JCheckBox) comp);
      }
      if (comp instanceof JLabel) {
        panelFunctionExecutionsSelecteds.remove((JLabel) comp);
      }


    }

    int[] selectedRows = tableExp2.getSelectedRows();
    HashMap<String, String[]> map = new HashMap<>();

    for (int i = 0; i < selectedRows.length; i++) {
      String experimentId = tableExp2.getModel().getValueAt(selectedRows[i], 0).toString();
      map.put(experimentId, db.Database.getOrdenedObjectives(experimentId).split(" "));
    }

    //Validacao
    if (selectedRows.length <= 1) {
      JOptionPane.showMessageDialog(null, rb.getString("atLeastTwoExecution"));
      return;
    } else if (selectedRows.length > 5) {
      JOptionPane.showMessageDialog(null, rb.getString("maxExecutions"));
      return;
    } else if (!Validators.selectedsExperimentsHasTheSameObjectiveFunctions(map)) {
      JOptionPane.showMessageDialog(null, rb.getString("notSameFunctions"));
      return;
    }

    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      String experimentId = entry.getKey();
      String[] values = entry.getValue();
      panelFunctionExecutionsSelecteds.add(new JLabel(""), "wrap");
      panelFunctionExecutionsSelecteds.add(new JLabel("Execution: " + experimentId + "\n"), "wrap");
      for (int i = 0; i < values.length; i++) {
        JCheckBox box = new JCheckBox(values[i].toUpperCase());
        box.setName(experimentId + "," + values[i] + "," + i); // id do experimemto, nome da funcao, indice
        panelFunctionExecutionsSelecteds.add(box, "span, grow");
      }
    }

    panelFunctionExecutionsSelecteds.updateUI();

  }//GEN-LAST:event_jButton2ActionPerformed

  private void btnGenerateChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateChartActionPerformed
    String referenceExp = null;
    List<JCheckBox> allChecks = new ArrayList();
    List<JCheckBox> checkeds = new ArrayList();
    HashMap<String, String> experimentToAlgorithmUsed = new HashMap<>();

    for (Object comp : panelFunctionExecutionsSelecteds.getComponents()) {
      if (comp instanceof JCheckBox) {
        JCheckBox checkBox = ((JCheckBox) comp);
        if (checkBox.isSelected()) {
          checkeds.add(checkBox);
        }
        allChecks.add(checkBox);
      }
    }

    for (JCheckBox box : checkeds) {
      String id = box.getName().split(",")[0]; // experimentID
      referenceExp = id;
      String algorithmUsed = db.Database.getAlgoritmUsedToExperimentId(id);
      experimentToAlgorithmUsed.put(id, algorithmUsed);
    }
    if (Validators.hasMoreThatTwoFunctionsSelectedForSelectedExperiments(allChecks)) {
      JOptionPane.showMessageDialog(null, rb.getString("onlyTwoFunctions"));
    } else if (checkeds.isEmpty()) {
      JOptionPane.showMessageDialog(null, rb.getString("atLeastTwoFunctionPerSelectedExperiment"));
    } else if (Validators.validateCheckedsFunctions(allChecks)) {
      JOptionPane.showMessageDialog(null, rb.getString("sameFunctions"));
    } else {
      String[] functions = new String[2]; //x,y Axis
      int[] columns = new int[2]; // Quais colunas do arquivo deseja-se ler.

      for (int i = 0; i < 2; i++) {
        final String[] splited = checkeds.get(i).getName().split(",");
        columns[i] = Integer.parseInt(splited[2]);
        functions[i] = splited[1];
      }

      String outputDir = this.config.getConfig().getDirectoryToExportModels();
      try {
        ChartGenerate.generate(functions, experimentToAlgorithmUsed, columns, outputDir, referenceExp);
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
    }

  }//GEN-LAST:event_btnGenerateChartActionPerformed

  private void executionDescriptionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_executionDescriptionKeyTyped
    int numbersOfChars = executionDescription.getText().length();
    if (numbersOfChars > 10) {
      executionDescription.setText(executionDescription.getText().substring(0, 10));
    }
  }//GEN-LAST:event_executionDescriptionKeyTyped

  private void btnGenerateEdChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateEdChartActionPerformed

    int[] selectedRows = tableExp2.getSelectedRows();
    String ids[] = new String[selectedRows.length];

    for (int i = 0; i < selectedRows.length; i++) {
      ids[i] = tableExp2.getModel().getValueAt(selectedRows[i], 0).toString();
    }
    
    String name = db.Database.getPlaUsedToExperimentId(ids[0]);

    if (selectedRows.length >= 1) {
      String typeChart = GuiFile.getInstance().getEdChartType();
      if ("bar".equalsIgnoreCase(typeChart)) {
        EdBar edBar = new EdBar(ids, "Euclidean Distance ("+name+")");
        edBar.displayOnFrame();
      } else if ("line".equals(typeChart)) {
        EdLine edLine = new EdLine(ids, null);
        edLine.displayOnFrame();
      } else {
        JOptionPane.showMessageDialog(null, rb.getString("confEdChartInvalid"));
      }
    } else {
      JOptionPane.showMessageDialog(null, rb.getString("atLeastOneExecution"));
    }
  }//GEN-LAST:event_btnGenerateEdChartActionPerformed

  private void btnHypervolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHypervolumeActionPerformed
    try {
      int[] selectedRows = tableExp2.getSelectedRows();
      String ids[] = new String[selectedRows.length];
      String funcs = "";

      for (int i = 0; i < selectedRows.length; i++) {
        ids[i] = tableExp2.getModel().getValueAt(selectedRows[i], 0).toString();
        String functions = db.Database.getOrdenedObjectives(ids[i]);
        if (funcs.isEmpty()) {
          funcs = functions;
        } else if (!funcs.equalsIgnoreCase(functions)) {
          JOptionPane.showMessageDialog(null, rb.getString("notSameFunctions"));

          return;
        }

      }

      HypervolumeWindow hyperPanel = new HypervolumeWindow();
      hyperPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      if (VolatileConfs.hypervolumeNormalized()) {
        hyperPanel.setTitle("Hypervolume - Normalized");
      } else {
        hyperPanel.setTitle("Hypervolume - Non Normalized");
      }
      hyperPanel.pack();
      hyperPanel.setResizable(false);
      hyperPanel.setVisible(true);

      hyperPanel.loadData(ids);
    } catch (IOException ex) {
      Logger.getLogger().putLog(ex.getMessage(), Level.ERROR);
      JOptionPane.showMessageDialog(null, rb.getString("errorGenerateHypervolumeTable"));
    }

  }//GEN-LAST:event_btnHypervolumeActionPerformed

  private void hypervolumeNormalizedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hypervolumeNormalizedActionPerformed
    if (hypervolumeNormalized.isEnabled()) {
      VolatileConfs.enableHybervolumeNormalization();
    } else {
      VolatileConfs.disableHybervolumeNormalization();
    }
  }//GEN-LAST:event_hypervolumeNormalizedActionPerformed

  private String fileChooser(JTextField fieldToSet, String allowExtension) throws HeadlessException {
    JFileChooser c = new JFileChooser();
    int rVal = c.showOpenDialog(this);

    if (rVal == JFileChooser.APPROVE_OPTION) {
      File f = new File(c.getCurrentDirectory() + c.getSelectedFile().getName());
      String ext = Utils.getExtension(f);

      if (!ext.equalsIgnoreCase(allowExtension)) {
        JOptionPane.showMessageDialog(null, "The selected file is not allowed. You need selects a file with extension .uml, but you selects a ." + ext + " file");
        return "";
      } else {
        final String path = c.getCurrentDirectory() + "/" + c.getSelectedFile().getName();
        fieldToSet.setText(path);
        fieldToSet.updateUI();
        return path;
      }
    }

    return "";
  }
  /**
   * @param args the command line arguments
   */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ApplicationConfs;
    private javax.swing.JPanel algorithms;
    private javax.swing.JButton bestSolutions;
    private javax.swing.JButton btnCleanListArchs1;
    private javax.swing.JButton btnConcernProfile;
    private javax.swing.JButton btnGenerateChart;
    private javax.swing.JButton btnGenerateEdChart;
    private javax.swing.JButton btnHypervolume;
    private javax.swing.JButton btnInput1;
    private javax.swing.JButton btnManipulationDir;
    private javax.swing.JButton btnOutput;
    private javax.swing.JButton btnPatternProfile;
    private javax.swing.JButton btnRelationshipProfile;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnShowConfigurations;
    private javax.swing.JButton btnSmartyProfile;
    private javax.swing.JButton btnTemplate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkAddClass;
    private javax.swing.JCheckBox checkBridge;
    private javax.swing.JCheckBox checkConcerns;
    private javax.swing.JCheckBox checkConventional;
    private javax.swing.JCheckBox checkCrossover;
    private javax.swing.JCheckBox checkElegance;
    private javax.swing.JCheckBox checkFeatureDriven;
    private javax.swing.JCheckBox checkFeatureMutation;
    private javax.swing.JCheckBox checkManagerClass;
    private javax.swing.JCheckBox checkMediator;
    private javax.swing.JCheckBox checkMoveAttribute;
    private javax.swing.JCheckBox checkMoveMethod;
    private javax.swing.JCheckBox checkMoveOperation;
    private javax.swing.JCheckBox checkMutation;
    private javax.swing.JCheckBox checkPLAExt;
    private javax.swing.JCheckBox checkPatterns;
    private javax.swing.JCheckBox checkRelationships;
    private javax.swing.JCheckBox checkSmarty;
    private javax.swing.JCheckBox checkStrategy;
    private javax.swing.JComboBox comboAlgorithms;
    private javax.swing.JComboBox comboMetrics;
    private javax.swing.JComboBox comboSolutions;
    private javax.swing.JSlider crossProbSlider;
    private javax.swing.JTextField executionDescription;
    private javax.swing.JPanel experiments;
    private javax.swing.JTextArea fieldArchitectureInput;
    private javax.swing.JTextField fieldConcernProfile;
    private javax.swing.JTextField fieldCrossoverProbability;
    private javax.swing.JTextField fieldManipulationDir;
    private javax.swing.JTextField fieldMaxEvaluations;
    private javax.swing.JTextField fieldMutationProb;
    private javax.swing.JTextField fieldNumberOfRuns;
    private javax.swing.JTextField fieldOutput;
    private javax.swing.JTextField fieldPaesArchiveSize;
    private javax.swing.JTextField fieldPatternsProfile;
    private javax.swing.JTextField fieldPopulationSize;
    private javax.swing.JTextField fieldRelationshipsProfile;
    private javax.swing.JTextField fieldSmartyProfile;
    private javax.swing.JTextField fieldTemplate;
    private javax.swing.JCheckBox hypervolumeNormalized;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelAlgorithms;
    private javax.swing.JLabel labelArchivePAES;
    private javax.swing.JLabel labelOperators;
    private javax.swing.JSlider mutatinProbSlider;
    private javax.swing.JPanel panelCrossProb;
    private javax.swing.JPanel panelExecutions;
    private javax.swing.JPanel panelExperimentSettings;
    private javax.swing.JPanel panelFunctionExecutionsSelecteds;
    private javax.swing.JPanel panelMetrics;
    private javax.swing.JPanel panelMutationProb;
    private javax.swing.JPanel panelObjectives;
    private javax.swing.JPanel panelOperatorsMutation;
    private javax.swing.JPanel panelPatternScope;
    private javax.swing.JPanel panelShowMetrics;
    private javax.swing.JPanel panelSolutions;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton radioElementsWithSameDPorNone;
    private javax.swing.JRadioButton radioRandomStrategy;
    private javax.swing.JTable tableExecutions;
    private javax.swing.JTable tableExp;
    private javax.swing.JTable tableExp2;
    private javax.swing.JTable tableMetrics;
    private javax.swing.JTable tableObjectives;

    // End of variables declaration//GEN-END:variables

  private void hidePanelMutationOperatorsByDefault() {
    panelOperatorsMutation.setVisible(false);
  }

  private void checkAllMutationOperatorsByDefault() {
    checkAddClass.setSelected(true);
    checkFeatureMutation.setSelected(true);
    checkManagerClass.setSelected(true);
    checkMoveAttribute.setSelected(true);
    checkMoveMethod.setSelected(true);
    checkMoveOperation.setSelected(true);

    FeatureMutationOperators[] operators = FeatureMutationOperators.values();
    for (FeatureMutationOperators operator : operators) {
      if (!operator.getOperatorName().equals("DesignPatterns")) {
        MutationOperatorsSelected.getSelectedMutationOperators().add(operator.getOperatorName());
      }
    }
  }

  private void hidePanelCrossoverProbabilityByDefault() {
    panelCrossProb.setVisible(false);
  }

  private void hidePanelMutationProbabilityByDefault() {
    panelMutationProb.setVisible(false);
  }

  private void checkAllMetricsByDefault() {
    for (Metrics m : Metrics.values()) {
      VolatileConfs.getObjectiveFunctionSelected().add(m.getName());
    }

    checkElegance.setSelected(true);
    checkPLAExt.setSelected(true);
    checkConventional.setSelected(true);
    checkFeatureDriven.setSelected(true);
  }

  /**
   * Somente faz uma copia do banco de dados vazio para a pasta da oplatool no
   * diretorio do usaurio se o mesmo nao existir.
   *
   */
  private void configureDb() {
    final String pathDb = UserHome.getPathToDb();

    if (!(new File(pathDb).exists())) {
      File dirDb = new File(UserHome.getOplaUserHome() + "db");
      if (!dirDb.exists()) {
        dirDb.mkdirs();
      }

      Utils.copy("emptyDB/oplatool.db", pathDb);
    }
    try {
      db.Database.setContent(results.Experiment.all());
    } catch (SQLException ex) {
      Logger.getLogger().putLog(String.format(String.format(String.format("Error ConfigureDB %s", ex.getMessage())),
              Level.INFO, main.class.getName()));
      System.exit(1);
    } catch (Exception ex) {
      Logger.getLogger().putLog(String.format(String.format(String.format(String.format("Generic ERROR %s", ex.getMessage()))),
              Level.INFO, main.class.getName()));
      System.exit(1);
    }

  }

  private void initiExecutedExperiments() {
    populateTables();
  }

  private void hidePanelSolutionsByDefault() {
    panelSolutions.setVisible(false);
    panelObjectives.setVisible(false);
  }

  private void disableFieldsOnStart() {
    fieldNumberOfRuns.setEnabled(false);
    fieldMaxEvaluations.setEnabled(false);
    fieldPopulationSize.setEnabled(false);
    fieldPaesArchiveSize.setEnabled(false);
  }

  private void enableFieldForPaes() {
    fieldNumberOfRuns.setEnabled(true);
    fieldMaxEvaluations.setEnabled(true);
    fieldPaesArchiveSize.setEnabled(true);
  }

  private void hideFieldsForNSGAII() {
    fieldPopulationSize.setEnabled(false);
  }

  private void enableFieldsForNSGAII() {
    fieldNumberOfRuns.setEnabled(true);
    fieldMaxEvaluations.setEnabled(true);
    fieldPopulationSize.setEnabled(true);
  }

  private void hideFieldsForPases() {
    fieldPaesArchiveSize.setEnabled(false);
  }

  private void hidePanelShowMetricsByDefault() {
    panelShowMetrics.setVisible(false);
  }
  ChangeListener changeListener = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
      JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
      int index = sourceTabbedPane.getSelectedIndex();
      String tabName = sourceTabbedPane.getTitleAt(index);
      if ("Executed Experiments".equalsIgnoreCase(tabName)) {
        if (db.Database.getContent().isEmpty()) {
          JOptionPane.showMessageDialog(null, "No experiment executed yet. ", "OPLA-Tool", 0);
        } else {
          db.Database.reloadContent();
          initiExecutedExperiments();
        }
      }

    }
  };

  private boolean noneDesignPatternsSelected() {
    return (!checkMediator.isSelected() && !checkStrategy.isSelected() && !checkBridge.isSelected());
  }

  private void blockUITabs() {
    jTabbedPane1.setEnabledAt(0, false);
    jTabbedPane1.setEnabledAt(1, false);
    jTabbedPane1.setEnabledAt(2, false);
    jTabbedPane1.setEnabledAt(3, false);
    jTabbedPane1.setEnabledAt(4, false);
  }

  private void populateTables() {
    JTable tables[] = {tableExp, tableExp2};
    List<results.Experiment> allExp = db.Database.getContent();

    for (int i = 0; i < tables.length; i++) {
      try {
        GuiUtils.makeTableNotEditable(tables[i]);
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("PLA");
        model.addColumn("Algorithm");
        model.addColumn("Created at");
        tables[i].setModel(model);

        for (results.Experiment exp : allExp) {
          Object[] row = new Object[4];
          row[0] = exp.getId();
          row[1] = exp.getName();
          row[2] = exp.getAlgorithmAndDescription();
          row[3] = exp.getCreatedAt();
          model.addRow(row);
        }
      } catch (Exception ex) {
        Logger.getLogger().putLog(String.format(String.format(String.format(String.format("Generic ERROR %s", ex.getMessage()))),
                Level.INFO, main.class.getName()));
      }
    }
  }

  private String titleWindow() {
    return "Execution " + this.selectedExperiment + " (" + db.Database.getAlgoritmUsedToExperimentId(this.selectedExperiment) + ") - " + db.Database.getPlaUsedToExperimentId(this.selectedExperiment);
  }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.gui2;

import com.ufpr.br.opla.algorithms.Solution;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author elf
 */
public class SolutionsComboBoxModel extends AbstractListModel implements ComboBoxModel {
    
    List<Solution> solutions =  new ArrayList<>();
    
    Solution selection = null;

    SolutionsComboBoxModel(String executionId, List<String> solutions) {
        for(String f : solutions)
            this.solutions.add(new Solution(executionId, f)); 
    }

    @Override
    public int getSize() {
        return solutions.size();
    }

    @Override
    public Object getElementAt(int index) {
        return solutions.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (Solution) anItem;
    }

    @Override
    public Object getSelectedItem() {
       return selection;
    }
    
    
}

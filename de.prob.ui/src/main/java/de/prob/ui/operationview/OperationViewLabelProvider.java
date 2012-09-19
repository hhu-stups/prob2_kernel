package de.prob.ui.operationview;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Joiner;

import de.prob.animator.domainobjects.OpInfo;
import de.prob.ui.Activator;

class OperationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private final Image imgEnabled = Activator.getDefault()
			.getImageRegistry().getDescriptor(Activator.IMG_ENABLED)
			.createImage();
	
	
	public String getColumnText(Object obj, int index) {
		if(index == 0) {
			if(obj instanceof OpInfo) {
				OpInfo op = (OpInfo) obj;
				return op.name;
			} else {
				return obj.getClass().toString();
			}
		}
		
		if(index == 1) {
			if(obj instanceof OpInfo) {
				OpInfo op = (OpInfo) obj;
				return Joiner.on(",").join(op.params);
			} else {
				return obj.getClass().toString();
			}
		}
		return "";
	}
	public Image getColumnImage(Object obj, int index) {
		if( index == 1 )
			return null;
		return getImage(obj);
	}
	public Image getImage(Object obj) {
		return imgEnabled;
	}
}
/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.bmotionstudio.core.editor.editpolicy;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import de.bmotionstudio.core.editor.command.ConnectionCreateCommand;
import de.bmotionstudio.core.editor.command.ConnectionReconnectCommand;
import de.bmotionstudio.core.editor.figure.TrackNodeFigure;
import de.bmotionstudio.core.editor.part.BMSAbstractEditPart;
import de.bmotionstudio.core.model.control.BConnection;
import de.bmotionstudio.core.model.control.Segment;
import de.bmotionstudio.core.model.control.TrackNode;

public class TrackEditPolicy extends BMSConnectionEditPolicy {

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {

		Command cmd = null;

		Object newObject = request.getNewObject();
		if (newObject instanceof Segment) {

			Object model = getHost().getModel();
			if (model instanceof TrackNode) {

				Segment track = (Segment) newObject;
				TrackNode trackNode = (TrackNode) model;
				track.setParent(trackNode.getVisualization());
				cmd = new ConnectionCreateCommand(trackNode);
				((ConnectionCreateCommand) cmd).setConnection(track);
				trackNode.getVisualization().getConnections()
						.put(track.getID(), track);
				request.setStartCommand(cmd);

			}

		} else if (newObject instanceof BConnection) {
			cmd = super.getConnectionCreateCommand(request);
		}

		return cmd;

	}

	@Override
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {

		Command cmd = null;

		Object newObject = request.getNewObject();
		if (newObject instanceof Segment) {

			cmd = request.getStartCommand();
			((ConnectionCreateCommand) cmd).setTarget((TrackNode) getHost()
					.getModel());

		} else if (newObject instanceof BConnection) {
			cmd = super.getConnectionCompleteCommand(request);
		}

		return cmd;

	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {

		Command cmd = null;

		Object newObject = request.getConnectionEditPart().getModel();
		if (newObject instanceof Segment) {

			Segment track = (Segment) newObject;
			TrackNode newSource = (TrackNode) getHost().getModel();
			cmd = new ConnectionReconnectCommand();
			((ConnectionReconnectCommand) cmd).setNewSource(newSource);
			((ConnectionReconnectCommand) cmd).setConnection(track);

		} else if (newObject instanceof BConnection) {
			cmd = super.getReconnectSourceCommand(request);
		}

		return cmd;

	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {

		Command cmd = null;

		Object newObject = request.getConnectionEditPart().getModel();
		if (newObject instanceof Segment) {

			Segment track = (Segment) newObject;
			TrackNode newTarget = (TrackNode) getHost().getModel();
			cmd = new ConnectionReconnectCommand();
			((ConnectionReconnectCommand) cmd).setNewTarget(newTarget);
			((ConnectionReconnectCommand) cmd).setConnection(track);

		} else if (newObject instanceof BConnection) {
			cmd = super.getReconnectTargetCommand(request);
		}

		return cmd;

	}

	@Override
	protected void showTargetConnectionFeedback(DropRequest request) {
		if (getHost() instanceof BMSAbstractEditPart) {
			BMSAbstractEditPart host = (BMSAbstractEditPart) getHost();
			IFigure figure = host.getFigure();
			if(figure instanceof TrackNodeFigure)
				((TrackNodeFigure)figure).setShowFeedback(true);
		}
	}

	@Override
	protected void eraseTargetConnectionFeedback(DropRequest request) {
		if (getHost() instanceof BMSAbstractEditPart) {
			BMSAbstractEditPart host = (BMSAbstractEditPart) getHost();
			IFigure figure = host.getFigure();
			if(figure instanceof TrackNodeFigure)
				((TrackNodeFigure)figure).setShowFeedback(false);
		}
	}

}

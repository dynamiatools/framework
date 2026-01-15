/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.modules.importer;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.ProgressEvent;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.integration.ProgressMonitorListener;
import tools.dynamia.modules.importer.ui.Importer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.LongOperation;

public abstract class ImportOperation extends LongOperation implements ProgressMonitorListener {

    private ProgressMonitor monitor;
    private Importer importer;
    private String name;
    private long updateProgressRate = 3000;
    private long lastCheckTime;
    private long currentTime;
    private boolean cancelledGracefully;
    private long startTime = 0;

    public ImportOperation(String name, Importer importer) {
        super();
        this.name = name;
        this.importer = importer;
        init();
    }

    public ImportOperation(String name, Importer importer, long updateProgressRate) {
        super();
        this.name = name;
        this.importer = importer;
        this.updateProgressRate = updateProgressRate;
        init();
    }

    private void init() {
        execute(this::doImport);
        onFinish(this::importFinish);
    }

    @Override
    public void progressChanged(ProgressEvent evt) {
        try {

            currentTime = System.currentTimeMillis();

            if (lastCheckTime == 0) {
                lastCheckTime = currentTime;
            }

            long elapsedTime = currentTime - startTime;
            if ((currentTime - lastCheckTime) >= updateProgressRate) {
                lastCheckTime = currentTime;

                double position = monitor.getCurrent();
                double total = monitor.getMax();

                if (startTime == 0) {
                    startTime = currentTime;
                }

                long estimatedRemaining = (long) (elapsedTime / position * (total - position));

                activate();
                monitor.setMessage(monitor.getMessage() + " - Faltan <b>"
                        + StringUtils.formatDuration(estimatedRemaining) + "</b> (h:m:s)");
                importer.updateProgress(monitor);
                deactivate();
            }
        } catch (Exception e) {
            // Ignore

        }
    }

    private void doImport() {
        monitor = new ProgressMonitor(this);
        try {
            if (checkCurrentOperation()) {

                importer.setCurrentOperation(this);
                setOperationStatus(true);

                execute(monitor);

                importer.setCurrentOperation(null);
                setOperationStatus(false);
            } else {
                setOperationStatus(false);
                cancel();
            }
        } catch (InterruptedException e) {
            throw new ImportOperationException(e);
        } catch (ValidationError e) {
            try {
                activate();
                UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
                importer.setOperationStatus(false);
                deactivate();
            } catch (Exception e2) {
                // TODO: handle exception
            }
        } catch (Exception e) {
            throw new ImportOperationException(e);
        }
    }

    public void cancelGracefully() {
        cancelledGracefully = true;
        monitor.setMax(-1);
        monitor.setCurrent(-1);
        monitor.stop();
    }

    private void importFinish() {
        if (!cancelledGracefully) {
            onFinish(monitor);
        } else {
            onCancel(monitor);
        }
    }

    protected void onFinish(ProgressMonitor monitor) {
        // TODO Auto-generated method stub
    }

    protected void onCancel(ProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }

    private void setOperationStatus(boolean b) {
        try {
            activate();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        importer.setOperationStatus(b);
        deactivate();
    }

    public String getName() {
        return name;
    }

    private boolean checkCurrentOperation() {
        if (importer.getCurrentOperation() != null) {
            UIMessages.showMessage("No puede ejecutar este proceso, existe una operacion de importacion activa "
                    + importer.getCurrentOperation().getName(), null);
            return false;
        } else {
            return true;
        }
    }

    public abstract void execute(ProgressMonitor monitor) throws Exception;

}

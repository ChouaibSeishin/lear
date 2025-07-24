

import { ImportReport } from './import-report';

export interface FullImportReport {
  [sheetName: string]: ImportReport;
}

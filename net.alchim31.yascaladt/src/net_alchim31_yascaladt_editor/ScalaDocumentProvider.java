package net_alchim31_yascaladt_editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ScalaDocumentProvider extends FileDocumentProvider {

    protected IDocument createDocument(Object element) throws CoreException {
        IDocument document = super.createDocument(element);
        if (document != null) {
            IDocumentPartitioner partitioner = new FastPartitioner(new Scanner4Partition(), Scanner4Partition.PARTITION_TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return document;
    }
}
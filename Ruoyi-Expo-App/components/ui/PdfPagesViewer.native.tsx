import { NativePdfPreview } from '@/components/ui/NativePdfPreview';

type Props = {
  uri: string;
};

export function PdfPagesViewer({ uri }: Props) {
  return <NativePdfPreview uri={uri} />;
}

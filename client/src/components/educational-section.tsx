import { useState } from "react";
import { School, ChevronDown, ChevronUp, CheckCircle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";

interface EducationalSectionProps {}

export default function EducationalSection({}: EducationalSectionProps) {
  const [openSections, setOpenSections] = useState<{ [key: string]: boolean }>({});

  const toggleSection = (sectionId: string) => {
    setOpenSections(prev => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const educationalContent = [
    {
      id: "what-is-pdu",
      title: "What is SMS PDU?",
      content: (
        <p className="text-gray-600 text-sm leading-relaxed">
          SMS PDU (Protocol Data Unit) is the raw format used by mobile networks to transmit SMS messages. 
          It contains all the technical information about the message including sender, recipient, timestamp, 
          message content, and various control fields encoded in hexadecimal format.
        </p>
      )
    },
    {
      id: "pdu-structure",
      title: "PDU Structure",
      content: (
        <div className="text-sm text-gray-600 space-y-2">
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">SMSC</span>
            <span>Service Center address information</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">PDU-Type</span>
            <span>Message type and flags</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">Sender</span>
            <span>Originating address</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">PID</span>
            <span>Protocol identifier</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">DCS</span>
            <span>Data coding scheme</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">SCTS</span>
            <span>Service center time stamp</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">UDL</span>
            <span>User data length</span>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-20 font-mono text-xs bg-white px-2 py-1 rounded border">UD</span>
            <span>User data (message content)</span>
          </div>
        </div>
      )
    },
    {
      id: "use-cases",
      title: "Common Use Cases",
      content: (
        <ul className="text-sm text-gray-600 space-y-2">
          <li className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            Telecom network debugging and troubleshooting
          </li>
          <li className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            SMS gateway development and testing
          </li>
          <li className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            Mobile application development
          </li>
          <li className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            Network protocol analysis
          </li>
          <li className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            Educational purposes and learning
          </li>
        </ul>
      )
    }
  ];

  return (
    <Card className="mb-6">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <School className="h-5 w-5 text-orange-600" />
          About SMS PDU Format
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {educationalContent.map((section) => (
          <Collapsible
            key={section.id}
            open={openSections[section.id]}
            onOpenChange={() => toggleSection(section.id)}
          >
            <CollapsibleTrigger asChild>
              <Button
                variant="ghost"
                className="w-full justify-between p-4 h-auto border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <span className="font-medium text-gray-900">{section.title}</span>
                {openSections[section.id] ? (
                  <ChevronUp className="h-4 w-4 text-gray-500" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-gray-500" />
                )}
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent>
              <div className="p-4 border-t border-gray-200 bg-gray-50 rounded-b-lg">
                {section.content}
              </div>
            </CollapsibleContent>
          </Collapsible>
        ))}
      </CardContent>
    </Card>
  );
}

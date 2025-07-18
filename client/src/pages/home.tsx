import { useState } from "react";
import { MessageSquare, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import PduAnalyzer from "@/components/pdu-analyzer";
import EducationalSection from "@/components/educational-section";
import SamplePduSection from "@/components/sample-pdu-section";

export default function Home() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-md sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="bg-primary text-white p-2 rounded-full">
                <MessageSquare className="h-6 w-6" />
              </div>
              <div>
                <h1 className="text-xl font-medium text-gray-900">SMS PDU Analyzer</h1>
                <p className="text-sm text-gray-600">Extract and parse SMS PDU data</p>
              </div>
            </div>
            <Sheet>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent>
                <div className="mt-6 space-y-4">
                  <h2 className="text-lg font-semibold">Navigation</h2>
                  <div className="space-y-2">
                    <Button variant="ghost" className="w-full justify-start">
                      Analyzer
                    </Button>
                    <Button variant="ghost" className="w-full justify-start">
                      About PDU
                    </Button>
                    <Button variant="ghost" className="w-full justify-start">
                      Samples
                    </Button>
                  </div>
                </div>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-6 max-w-4xl">
        <PduAnalyzer />
        <EducationalSection />
        <SamplePduSection />
      </main>
    </div>
  );
}
